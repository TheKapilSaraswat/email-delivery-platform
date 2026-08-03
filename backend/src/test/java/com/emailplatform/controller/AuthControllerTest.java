package com.emailplatform.controller;

import com.emailplatform.dto.AuthResponse;
import com.emailplatform.dto.LoginRequest;
import com.emailplatform.dto.RegisterRequest;
import com.emailplatform.model.User;
import com.emailplatform.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testRegisterSuccess() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("test@test.com");
        req.setPassword("password");
        req.setName("Test User");

        AuthResponse resp = new AuthResponse("token123", "user-id", "test@test.com", "Test User");
        when(authService.register(any(RegisterRequest.class))).thenReturn(resp);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token123"))
                .andExpect(jsonPath("$.email").value("test@test.com"))
                .andExpect(jsonPath("$.name").value("Test User"));
    }

    @Test
    void testRegisterFailure() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("existing@test.com");
        req.setPassword("password");
        req.setName("User");

        when(authService.register(any(RegisterRequest.class)))
            .thenThrow(new RuntimeException("Email already registered"));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Email already registered"));
    }

    @Test
    void testRegisterInvalidEmail() throws Exception {
        Map<String, String> req = Map.of("email", "bad", "password", "pass", "name", "Name");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRegisterMissingFields() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testLoginSuccess() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail("test@test.com");
        req.setPassword("password");

        AuthResponse resp = new AuthResponse("token123", "user-id", "test@test.com", "Test User");
        when(authService.login(any(LoginRequest.class))).thenReturn(resp);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token123"));
    }

    @Test
    void testLoginFailure() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail("wrong@test.com");
        req.setPassword("wrong");

        when(authService.login(any(LoginRequest.class)))
            .thenThrow(new RuntimeException("Invalid credentials"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid credentials"));
    }

    @Test
    void testLoginInvalidEmail() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"bad\",\"password\":\"pass\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testLoginMissingFields() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
