package com.emailplatform.controller;

import com.emailplatform.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/campaigns/{id}")
    public ResponseEntity<?> getCampaignStats(@AuthenticationPrincipal String userId,
                                               @PathVariable String id) {
        try {
            Map<String, Object> stats = analyticsService.getCampaignStats(id, userId);
            return ResponseEntity.ok(stats);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/overview")
    public ResponseEntity<?> getOverview(@AuthenticationPrincipal String userId) {
        Map<String, Object> overview = analyticsService.getOverview(userId);
        return ResponseEntity.ok(overview);
    }
}
