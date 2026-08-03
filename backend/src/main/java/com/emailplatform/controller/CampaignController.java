package com.emailplatform.controller;

import com.emailplatform.dto.CampaignRequest;
import com.emailplatform.model.Campaign;
import com.emailplatform.service.CampaignService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/campaigns")
public class CampaignController {

    private final CampaignService campaignService;

    public CampaignController(CampaignService campaignService) {
        this.campaignService = campaignService;
    }

    @GetMapping
    public ResponseEntity<List<Campaign>> getCampaigns(@AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(campaignService.getCampaigns(userId));
    }

    @PostMapping
    public ResponseEntity<?> createCampaign(@AuthenticationPrincipal String userId,
                                             @Valid @RequestBody CampaignRequest request) {
        try {
            Campaign campaign = campaignService.createCampaign(userId, request);
            return ResponseEntity.ok(campaign);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCampaign(@AuthenticationPrincipal String userId,
                                             @PathVariable String id,
                                             @Valid @RequestBody CampaignRequest request) {
        try {
            Campaign campaign = campaignService.updateCampaign(id, userId, request);
            return ResponseEntity.ok(campaign);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCampaign(@AuthenticationPrincipal String userId,
                                             @PathVariable String id) {
        try {
            campaignService.deleteCampaign(id, userId);
            return ResponseEntity.ok(Map.of("message", "Campaign deleted"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/send")
    public ResponseEntity<?> sendCampaign(@AuthenticationPrincipal String userId,
                                           @PathVariable String id) {
        try {
            campaignService.sendCampaign(id, userId);
            return ResponseEntity.ok(Map.of("message", "Campaign sending started"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/schedule")
    public ResponseEntity<?> scheduleCampaign(@AuthenticationPrincipal String userId,
                                               @PathVariable String id,
                                               @RequestBody Map<String, String> body) {
        try {
            java.time.LocalDateTime scheduledAt = java.time.LocalDateTime.parse(body.get("scheduledAt"));
            campaignService.scheduleCampaign(id, userId, scheduledAt);
            return ResponseEntity.ok(Map.of("message", "Campaign scheduled"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
