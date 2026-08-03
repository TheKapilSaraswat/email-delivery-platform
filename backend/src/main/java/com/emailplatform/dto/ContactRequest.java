package com.emailplatform.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ContactRequest {
    @Email @NotBlank
    private String email;
    private String firstName;
    private String lastName;
    private String list;
    private String metadata;
}
