package com.emailplatform.service;

import com.emailplatform.dto.ApiKeyRequest;
import com.emailplatform.model.ApiKey;
import com.emailplatform.repository.ApiKeyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApiKeyServiceTest {

    @Mock
    private ApiKeyRepository apiKeyRepository;

    @InjectMocks
    private ApiKeyService apiKeyService;

    private ApiKey testKey;

    @BeforeEach
    void setUp() {
        testKey = new ApiKey();
        testKey.setId("key-id-1");
        testKey.setKeyValue("ep_abc123");
        testKey.setName("Test Key");
        testKey.setUserId("user-id-1");
        testKey.setActive(true);
    }

    @Test
    void testGetApiKeys() {
        when(apiKeyRepository.findByUserId("user-id-1")).thenReturn(Arrays.asList(testKey));

        List<ApiKey> result = apiKeyService.getApiKeys("user-id-1");

        assertEquals(1, result.size());
        assertEquals("Test Key", result.get(0).getName());
    }

    @Test
    void testGetApiKeysEmpty() {
        when(apiKeyRepository.findByUserId("user-id-1")).thenReturn(Arrays.asList());

        List<ApiKey> result = apiKeyService.getApiKeys("user-id-1");

        assertTrue(result.isEmpty());
    }

    @Test
    void testCreateApiKey() {
        ApiKeyRequest req = new ApiKeyRequest();
        req.setName("New Key");

        when(apiKeyRepository.save(any(ApiKey.class))).thenAnswer(inv -> {
            ApiKey k = inv.getArgument(0);
            k.setId("new-key-id");
            return k;
        });

        ApiKey result = apiKeyService.createApiKey("user-id-1", req);

        assertNotNull(result);
        assertEquals("New Key", result.getName());
        assertTrue(result.getKeyValue().startsWith("ep_"));
        assertTrue(result.isActive());
        assertEquals("user-id-1", result.getUserId());
        verify(apiKeyRepository).save(any(ApiKey.class));
    }

    @Test
    void testCreateApiKeyKeyValueUnique() {
        ApiKeyRequest req1 = new ApiKeyRequest();
        req1.setName("Key 1");
        ApiKeyRequest req2 = new ApiKeyRequest();
        req2.setName("Key 2");

        when(apiKeyRepository.save(any(ApiKey.class))).thenAnswer(inv -> {
            ApiKey k = inv.getArgument(0);
            k.setId("key-" + System.nanoTime());
            return k;
        });

        ApiKey result1 = apiKeyService.createApiKey("user-id-1", req1);
        ApiKey result2 = apiKeyService.createApiKey("user-id-1", req2);

        assertNotEquals(result1.getKeyValue(), result2.getKeyValue());
    }

    @Test
    void testRevokeApiKeySuccess() {
        when(apiKeyRepository.findById("key-id-1")).thenReturn(Optional.of(testKey));
        when(apiKeyRepository.save(any(ApiKey.class))).thenReturn(testKey);

        apiKeyService.revokeApiKey("key-id-1", "user-id-1");

        assertFalse(testKey.isActive());
        verify(apiKeyRepository).save(testKey);
    }

    @Test
    void testRevokeApiKeyNotFound() {
        when(apiKeyRepository.findById("nonexistent")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> apiKeyService.revokeApiKey("nonexistent", "user-id-1"));
        assertEquals("API key not found", ex.getMessage());
    }

    @Test
    void testRevokeApiKeyUnauthorized() {
        when(apiKeyRepository.findById("key-id-1")).thenReturn(Optional.of(testKey));

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> apiKeyService.revokeApiKey("key-id-1", "wrong-user"));
        assertEquals("Unauthorized", ex.getMessage());
    }

    @Test
    void testGetMultipleApiKeys() {
        ApiKey k2 = new ApiKey();
        k2.setId("key-id-2");
        k2.setName("Key 2");
        k2.setUserId("user-id-1");

        when(apiKeyRepository.findByUserId("user-id-1")).thenReturn(Arrays.asList(testKey, k2));

        List<ApiKey> result = apiKeyService.getApiKeys("user-id-1");

        assertEquals(2, result.size());
    }

    @Test
    void testCreateApiKeyKeyValueFormat() {
        ApiKeyRequest req = new ApiKeyRequest();
        req.setName("Format Key");

        when(apiKeyRepository.save(any(ApiKey.class))).thenAnswer(inv -> {
            ApiKey k = inv.getArgument(0);
            k.setId("fmt-id");
            return k;
        });

        ApiKey result = apiKeyService.createApiKey("user-id-1", req);

        assertTrue(result.getKeyValue().startsWith("ep_"));
        assertTrue(result.getKeyValue().length() > 3);
        assertFalse(result.getKeyValue().contains("-"));
    }
}
