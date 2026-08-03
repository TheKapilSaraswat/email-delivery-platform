package com.emailplatform.controller;

import com.emailplatform.dto.EmailSendRequest;
import com.emailplatform.model.ApiKey;
import com.emailplatform.model.User;
import com.emailplatform.repository.ApiKeyRepository;
import com.emailplatform.repository.UserRepository;
import com.emailplatform.service.EmailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SendController.class)
@AutoConfigureMockMvc(addFilters = false)
class SendControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ApiKeyRepository apiKeyRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private EmailService emailService;

    @Autowired
    private ObjectMapper objectMapper;

    private ApiKey createTestKey() {
        ApiKey k = new ApiKey();
        k.setId("key-1");
        k.setKeyValue("ep_testkey123");
        k.setUserId("user-1");
        k.setActive(true);
        return k;
    }

    private User createTestUser(String role) {
        User u = new User();
        u.setId("user-1");
        u.setEmail("owner@test.com");
        u.setName("Owner");
        u.setRole(role);
        return u;
    }

    @Test
    void testSendEmailDemoModeForRegularUser() throws Exception {
        EmailSendRequest req = new EmailSendRequest();
        req.setTo("recipient@test.com");
        req.setSubject("Test Subject");
        req.setBody("<p>Test body</p>");

        when(apiKeyRepository.findByKeyValue(any())).thenReturn(Optional.of(createTestKey()));
        when(userRepository.findById("user-1")).thenReturn(Optional.of(createTestUser("USER")));

        mockMvc.perform(post("/api/send")
                .header("x-api-key", "ep_testkey123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Email sent in demo mode (no real email was sent)"));

        verify(emailService).sendEmailSimulated(anyString(), anyString(), anyString());
        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void testSendEmailRealForAdmin() throws Exception {
        EmailSendRequest req = new EmailSendRequest();
        req.setTo("recipient@test.com");
        req.setSubject("Test Subject");
        req.setBody("<p>Test body</p>");

        when(apiKeyRepository.findByKeyValue(any())).thenReturn(Optional.of(createTestKey()));
        when(userRepository.findById("user-1")).thenReturn(Optional.of(createTestUser("ADMIN")));

        mockMvc.perform(post("/api/send")
                .header("x-api-key", "ep_testkey123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Email sent"));

        verify(emailService).sendEmail(anyString(), anyString(), anyString());
        verify(emailService, never()).sendEmailSimulated(anyString(), anyString(), anyString());
    }

    @Test
    void testSendEmailInvalidApiKey() throws Exception {
        EmailSendRequest req = new EmailSendRequest();
        req.setTo("recipient@test.com");
        req.setSubject("Test");
        req.setBody("<p>Body</p>");

        when(apiKeyRepository.findByKeyValue("invalid")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/send")
                .header("x-api-key", "invalid")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid API key"));
    }

    @Test
    void testSendEmailRevokedKey() throws Exception {
        EmailSendRequest req = new EmailSendRequest();
        req.setTo("recipient@test.com");
        req.setSubject("Test");
        req.setBody("<p>Body</p>");

        ApiKey revokedKey = createTestKey();
        revokedKey.setActive(false);
        when(apiKeyRepository.findByKeyValue(any())).thenReturn(Optional.of(revokedKey));

        mockMvc.perform(post("/api/send")
                .header("x-api-key", "ep_testkey123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("API key is revoked"));
    }

    @Test
    void testSendEmailSmtpFailure() throws Exception {
        EmailSendRequest req = new EmailSendRequest();
        req.setTo("recipient@test.com");
        req.setSubject("Test");
        req.setBody("<p>Body</p>");

        when(apiKeyRepository.findByKeyValue(any())).thenReturn(Optional.of(createTestKey()));
        when(userRepository.findById("user-1")).thenReturn(Optional.of(createTestUser("ADMIN")));
        doThrow(new RuntimeException("SMTP error")).when(emailService).sendEmail(anyString(), anyString(), anyString());

        mockMvc.perform(post("/api/send")
                .header("x-api-key", "ep_testkey123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("SMTP error"));
    }

    @Test
    void testSendEmailMissingApiKey() throws Exception {
        EmailSendRequest req = new EmailSendRequest();
        req.setTo("recipient@test.com");
        req.setSubject("Test");
        req.setBody("<p>Body</p>");

        mockMvc.perform(post("/api/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testSendEmailInvalidBody() throws Exception {
        mockMvc.perform(post("/api/send")
                .header("x-api-key", "ep_testkey123")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"to\":\"bad\"}"))
                .andExpect(status().isBadRequest());
    }
}
