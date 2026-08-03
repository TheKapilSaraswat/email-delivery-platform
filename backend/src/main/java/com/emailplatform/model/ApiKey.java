package com.emailplatform.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "api_keys")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiKey {

    @Id
    private String id;

    @Column(nullable = false)
    private String keyValue;

    @Column(nullable = false)
    private String name;

    private String userId;
    private boolean active;
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (id == null) id = java.util.UUID.randomUUID().toString();
        if (createdAt == null) createdAt = LocalDateTime.now();
        active = true;
    }
}
