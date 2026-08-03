package com.emailplatform.service;

import com.emailplatform.dto.ApiKeyRequest;
import com.emailplatform.model.ApiKey;
import com.emailplatform.repository.ApiKeyRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;

    public ApiKeyService(ApiKeyRepository apiKeyRepository) {
        this.apiKeyRepository = apiKeyRepository;
    }

    public List<ApiKey> getApiKeys(String userId) {
        return apiKeyRepository.findByUserId(userId);
    }

    public ApiKey createApiKey(String userId, ApiKeyRequest request) {
        ApiKey apiKey = new ApiKey();
        apiKey.setName(request.getName());
        apiKey.setKeyValue("ep_" + UUID.randomUUID().toString().replace("-", ""));
        apiKey.setUserId(userId);
        apiKey.setActive(true);
        return apiKeyRepository.save(apiKey);
    }

    public void revokeApiKey(String id, String userId) {
        ApiKey apiKey = apiKeyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("API key not found"));
        if (!apiKey.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        apiKey.setActive(false);
        apiKeyRepository.save(apiKey);
    }
}
