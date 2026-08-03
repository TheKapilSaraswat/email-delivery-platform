package com.emailplatform.controller;

import com.emailplatform.dto.EmailSendRequest;
import com.emailplatform.model.ApiKey;
import com.emailplatform.model.User;
import com.emailplatform.repository.ApiKeyRepository;
import com.emailplatform.repository.UserRepository;
import com.emailplatform.service.EmailService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/send")
public class SendController {

    private final ApiKeyRepository apiKeyRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public SendController(ApiKeyRepository apiKeyRepository, UserRepository userRepository, EmailService emailService) {
        this.apiKeyRepository = apiKeyRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    @PostMapping
    public ResponseEntity<?> sendEmail(@RequestHeader("x-api-key") String apiKey,
                                        @Valid @RequestBody EmailSendRequest request) {
        try {
            ApiKey key = apiKeyRepository.findByKeyValue(apiKey)
                    .orElseThrow(() -> new RuntimeException("Invalid API key"));
            if (!key.isActive()) {
                throw new RuntimeException("API key is revoked");
            }
            User owner = userRepository.findById(key.getUserId()).orElse(null);
            boolean demoMode = owner == null || !"ADMIN".equals(owner.getRole());
            if (demoMode) {
                emailService.sendEmailSimulated(request.getTo(), request.getSubject(), request.getBody());
                return ResponseEntity.ok(Map.of("success", true, "message", "Email sent in demo mode (no real email was sent)"));
            }
            emailService.sendEmail(request.getTo(), request.getSubject(), request.getBody());
            return ResponseEntity.ok(Map.of("success", true, "message", "Email sent"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
