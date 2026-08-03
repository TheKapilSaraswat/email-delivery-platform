package com.emailplatform.repository;

import com.emailplatform.model.ApiKey;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ApiKeyRepositoryTest {

    @Autowired
    private ApiKeyRepository apiKeyRepository;

    @Test
    void testSaveAndFind() {
        ApiKey k = new ApiKey();
        k.setKeyValue("ep_test123");
        k.setName("Test Key");
        k.setUserId("user-1");
        k.setActive(true);
        ApiKey saved = apiKeyRepository.save(k);

        assertNotNull(saved.getId());
        assertTrue(apiKeyRepository.findById(saved.getId()).isPresent());
    }

    @Test
    void testFindByUserId() {
        ApiKey k1 = new ApiKey();
        k1.setKeyValue("ep_key1");
        k1.setName("Key 1");
        k1.setUserId("user-1");
        k1.setActive(true);
        apiKeyRepository.save(k1);

        ApiKey k2 = new ApiKey();
        k2.setKeyValue("ep_key2");
        k2.setName("Key 2");
        k2.setUserId("user-1");
        k2.setActive(true);
        apiKeyRepository.save(k2);

        ApiKey k3 = new ApiKey();
        k3.setKeyValue("ep_key3");
        k3.setName("Key 3");
        k3.setUserId("user-2");
        k3.setActive(true);
        apiKeyRepository.save(k3);

        List<ApiKey> result = apiKeyRepository.findByUserId("user-1");
        assertEquals(2, result.size());
    }

    @Test
    void testFindByUserIdEmpty() {
        List<ApiKey> result = apiKeyRepository.findByUserId("nonexistent");
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindByKeyValue() {
        ApiKey k = new ApiKey();
        k.setKeyValue("ep_unique123");
        k.setName("Unique Key");
        k.setUserId("user-1");
        k.setActive(true);
        apiKeyRepository.save(k);

        Optional<ApiKey> found = apiKeyRepository.findByKeyValue("ep_unique123");
        assertTrue(found.isPresent());
        assertEquals("Unique Key", found.get().getName());
    }

    @Test
    void testFindByKeyValueNotFound() {
        Optional<ApiKey> found = apiKeyRepository.findByKeyValue("ep_nonexistent");
        assertFalse(found.isPresent());
    }

    @Test
    void testDeleteApiKey() {
        ApiKey k = new ApiKey();
        k.setKeyValue("ep_delete");
        k.setName("Delete Me");
        k.setUserId("user-1");
        k.setActive(true);
        ApiKey saved = apiKeyRepository.save(k);

        apiKeyRepository.deleteById(saved.getId());
        assertFalse(apiKeyRepository.findById(saved.getId()).isPresent());
    }

    @Test
    void testCount() {
        assertEquals(0, apiKeyRepository.count());

        ApiKey k = new ApiKey();
        k.setKeyValue("ep_count");
        k.setName("Count");
        k.setUserId("user-1");
        k.setActive(true);
        apiKeyRepository.save(k);

        assertEquals(1, apiKeyRepository.count());
    }

    @Test
    void testActiveInactiveKeys() {
        ApiKey active = new ApiKey();
        active.setKeyValue("ep_active");
        active.setName("Active");
        active.setUserId("user-1");
        active.setActive(true);
        apiKeyRepository.save(active);

        ApiKey inactive = new ApiKey();
        inactive.setKeyValue("ep_inactive");
        inactive.setName("Inactive");
        inactive.setUserId("user-1");
        inactive.setActive(false);
        apiKeyRepository.save(inactive);

        List<ApiKey> result = apiKeyRepository.findByUserId("user-1");
        assertEquals(2, result.size());
    }
}
