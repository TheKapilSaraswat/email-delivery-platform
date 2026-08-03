package com.emailplatform.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CampaignRequest {
    @NotBlank
    private String name;
    private String description;
    private String templateId;
    private String contactList;
    private LocalDateTime scheduledAt;
}
