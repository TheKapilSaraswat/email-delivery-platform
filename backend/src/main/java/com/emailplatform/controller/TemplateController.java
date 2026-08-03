package com.emailplatform.controller;

import com.emailplatform.dto.TemplateRequest;
import com.emailplatform.model.Template;
import com.emailplatform.service.TemplateService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/templates")
public class TemplateController {

    private final TemplateService templateService;

    public TemplateController(TemplateService templateService) {
        this.templateService = templateService;
    }

    @GetMapping
    public ResponseEntity<List<Template>> getTemplates(@AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(templateService.getTemplates(userId));
    }

    @PostMapping
    public ResponseEntity<?> createTemplate(@AuthenticationPrincipal String userId,
                                             @Valid @RequestBody TemplateRequest request) {
        try {
            Template template = templateService.createTemplate(userId, request);
            return ResponseEntity.ok(template);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTemplate(@AuthenticationPrincipal String userId,
                                             @PathVariable String id,
                                             @Valid @RequestBody TemplateRequest request) {
        try {
            Template template = templateService.updateTemplate(id, userId, request);
            return ResponseEntity.ok(template);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTemplate(@AuthenticationPrincipal String userId,
                                             @PathVariable String id) {
        try {
            templateService.deleteTemplate(id, userId);
            return ResponseEntity.ok(Map.of("message", "Template deleted"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
