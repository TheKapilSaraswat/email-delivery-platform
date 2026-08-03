package com.emailplatform.controller;

import com.emailplatform.service.AnalyticsService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Base64;
import java.util.Map;

@RestController
@RequestMapping("/api/track")
public class TrackingController {

    private final AnalyticsService analyticsService;

    public TrackingController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping(value = "/open/{campaignId}/{contactId}", produces = MediaType.IMAGE_GIF_VALUE)
    public byte[] trackOpen(@PathVariable String campaignId,
                            @PathVariable String contactId) {
        analyticsService.trackEvent(campaignId, contactId, "open", null);
        return Base64.getDecoder().decode("R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7");
    }

    @GetMapping("/click/{campaignId}/{contactId}")
    public ResponseEntity<?> trackClick(@PathVariable String campaignId,
                                         @PathVariable String contactId,
                                         @RequestParam String url) {
        if (url == null || url.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "URL is required"));
        }
        String lowerUrl = url.toLowerCase();
        if (lowerUrl.startsWith("javascript:") || lowerUrl.startsWith("data:")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid URL scheme"));
        }
        if (!lowerUrl.startsWith("http://") && !lowerUrl.startsWith("https://")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Only HTTP/HTTPS URLs are allowed"));
        }
        analyticsService.trackEvent(campaignId, contactId, "click", url);
        return ResponseEntity.status(302).header("Location", url).build();
    }
}
