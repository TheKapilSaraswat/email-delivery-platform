package com.emailplatform.controller;

import com.emailplatform.dto.ApiKeyRequest;
import com.emailplatform.model.ApiKey;
import com.emailplatform.service.ApiKeyService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/api-keys")
public class ApiKeyController {

    private final ApiKeyService apiKeyService;

    public ApiKeyController(ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    @GetMapping
    public ResponseEntity<List<ApiKey>> getApiKeys(@AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(apiKeyService.getApiKeys(userId));
    }

    @PostMapping
    public ResponseEntity<?> createApiKey(@AuthenticationPrincipal String userId,
                                           @Valid @RequestBody ApiKeyRequest request) {
        try {
            ApiKey apiKey = apiKeyService.createApiKey(userId, request);
            return ResponseEntity.ok(apiKey);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> revokeApiKey(@AuthenticationPrincipal String userId,
                                           @PathVariable String id) {
        try {
            apiKeyService.revokeApiKey(id, userId);
            return ResponseEntity.ok(Map.of("message", "API key revoked"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
