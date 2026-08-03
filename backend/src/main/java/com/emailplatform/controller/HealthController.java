package com.emailplatform.controller;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.Instant;
import java.util.Map;

@RestController
public class HealthController {

    private final ObjectProvider<DataSource> dataSourceProvider;

    public HealthController(ObjectProvider<DataSource> dataSourceProvider) {
        this.dataSourceProvider = dataSourceProvider;
    }

    @GetMapping("/api/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "service", "email-platform",
                "timestamp", Instant.now().toString()
        ));
    }

    @GetMapping("/api/ready")
    public ResponseEntity<Map<String, Object>> readiness() {
        DataSource dataSource = dataSourceProvider.getIfAvailable();
        if (dataSource == null) {
            return ResponseEntity.status(503).body(Map.of(
                    "status", "not_ready",
                    "database", "unavailable",
                    "timestamp", Instant.now().toString()
            ));
        }
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(2)) {
                return ResponseEntity.ok(Map.of(
                        "status", "ready",
                        "database", "ok",
                        "timestamp", Instant.now().toString()
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(503).body(Map.of(
                    "status", "not_ready",
                    "database", "unreachable",
                    "error", e.getMessage()
            ));
        }
        return ResponseEntity.status(503).body(Map.of("status", "not_ready", "database", "unreachable"));
    }
}
