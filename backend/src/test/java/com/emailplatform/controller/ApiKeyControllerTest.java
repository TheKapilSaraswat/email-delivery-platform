package com.emailplatform.controller;

import com.emailplatform.dto.ApiKeyRequest;
import com.emailplatform.model.ApiKey;
import com.emailplatform.service.ApiKeyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ApiKeyController.class)
@AutoConfigureMockMvc(addFilters = false)
class ApiKeyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ApiKeyService apiKeyService;

    @Autowired
    private ObjectMapper objectMapper;

    private ApiKey createTestKey() {
        ApiKey k = new ApiKey();
        k.setId("key-id-1");
        k.setKeyValue("ep_abc123def");
        k.setName("Test Key");
        k.setUserId("user-id-1");
        k.setActive(true);
        k.setCreatedAt(LocalDateTime.now());
        return k;
    }

    @Test
    void testGetApiKeys() throws Exception {
        ApiKey k = createTestKey();
        when(apiKeyService.getApiKeys(any())).thenReturn(Arrays.asList(k));

        mockMvc.perform(get("/api/api-keys"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Key"));
    }

    @Test
    void testGetApiKeysEmpty() throws Exception {
        when(apiKeyService.getApiKeys(any())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/api-keys"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void testCreateApiKey() throws Exception {
        ApiKeyRequest req = new ApiKeyRequest();
        req.setName("New Key");

        ApiKey k = createTestKey();
        k.setName("New Key");
        when(apiKeyService.createApiKey(any(), any(ApiKeyRequest.class))).thenReturn(k);

        mockMvc.perform(post("/api/api-keys")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Key"))
                .andExpect(jsonPath("$.keyValue").value("ep_abc123def"));
    }

    @Test
    void testCreateApiKeyInvalidInput() throws Exception {
        mockMvc.perform(post("/api/api-keys")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRevokeApiKey() throws Exception {
        doNothing().when(apiKeyService).revokeApiKey(any(), any());

        mockMvc.perform(delete("/api/api-keys/key-id-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("API key revoked"));
    }

    @Test
    void testRevokeApiKeyNotFound() throws Exception {
        doNothing().when(apiKeyService).revokeApiKey(any(), any());

        mockMvc.perform(delete("/api/api-keys/nonexistent"))
                .andExpect(status().isOk());
    }
}
