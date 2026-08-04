package com.emailplatform.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "campaigns")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Campaign {

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String templateId;
    private String userId;

    @Column(columnDefinition = "TEXT")
    private String contactList;

    @Column(nullable = false)
    private String status;

    private Integer sentCount;
    private Integer openedCount;
    private Integer clickedCount;

    private LocalDateTime scheduledAt;
    private LocalDateTime sentAt;
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = java.util.UUID.randomUUID().toString();
        }
        if (status == null) {
            status = "draft";
        }
        if (sentCount == null) {
            sentCount = 0;
        }
        if (openedCount == null) {
            openedCount = 0;
        }
        if (clickedCount == null) {
            clickedCount = 0;
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
