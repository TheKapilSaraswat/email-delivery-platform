package com.emailplatform.service;

import com.emailplatform.dto.AuthResponse;
import com.emailplatform.dto.LoginRequest;
import com.emailplatform.dto.ProfileRequest;
import com.emailplatform.dto.RegisterRequest;
import com.emailplatform.model.User;
import com.emailplatform.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AuthService authService;

    private User testUser;
    private static final String JWT_SECRET = "test-secret-key-email-platform-2024-for-testing";
    private static final long EXPIRATION = 604800000L;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, JWT_SECRET, EXPIRATION, "admin@test.com");
        testUser = new User();
        testUser.setId("user-id-1");
        testUser.setEmail("test@test.com");
        testUser.setPassword("$2a$encoded");
        testUser.setName("Test User");
    }

    @Test
    void testRegisterSuccess() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("new@test.com");
        req.setPassword("password");
        req.setName("New User");

        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("$2a$encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId("new-id");
            return u;
        });

        AuthResponse response = authService.register(req);

        assertNotNull(response);
        assertNotNull(response.getToken());
        assertEquals("new@test.com", response.getEmail());
        assertEquals("New User", response.getName());
        verify(userRepository).existsByEmail("new@test.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testRegisterAdminEmailGetsAdminRole() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("admin@test.com");
        req.setPassword("password");
        req.setName("Admin User");

        when(userRepository.existsByEmail("admin@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("$2a$encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        AuthResponse response = authService.register(req);

        assertNotNull(response);
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("ADMIN", captor.getValue().getRole());
    }

    @Test
    void testRegisterNonAdminEmailGetsUserRole() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("regular@test.com");
        req.setPassword("password");
        req.setName("Regular User");

        when(userRepository.existsByEmail("regular@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("$2a$encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        authService.register(req);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("USER", captor.getValue().getRole());
    }

    @Test
    void testRegisterEmailAlreadyExists() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("existing@test.com");
        req.setPassword("password");
        req.setName("User");

        when(userRepository.existsByEmail("existing@test.com")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.register(req));
        assertEquals("An account with this email already exists. Please login instead.", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void testLoginSuccess() {
        LoginRequest req = new LoginRequest();
        req.setEmail("test@test.com");
        req.setPassword("password");

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password", "$2a$encoded")).thenReturn(true);

        AuthResponse response = authService.login(req);

        assertNotNull(response);
        assertNotNull(response.getToken());
        assertEquals("test@test.com", response.getEmail());
    }

    @Test
    void testLoginUserNotFound() {
        LoginRequest req = new LoginRequest();
        req.setEmail("nonexistent@test.com");
        req.setPassword("password");

        when(userRepository.findByEmail("nonexistent@test.com")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.login(req));
        assertEquals("Invalid email or password", ex.getMessage());
    }

    @Test
    void testLoginWrongPassword() {
        LoginRequest req = new LoginRequest();
        req.setEmail("test@test.com");
        req.setPassword("wrongpassword");

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpassword", "$2a$encoded")).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.login(req));
        assertEquals("Invalid email or password", ex.getMessage());
    }

    @Test
    void testGetCurrentUserSuccess() {
        when(userRepository.findById("user-id-1")).thenReturn(Optional.of(testUser));

        User result = authService.getCurrentUser("user-id-1");

        assertNotNull(result);
        assertEquals("test@test.com", result.getEmail());
    }

    @Test
    void testGetCurrentUserNotFound() {
        when(userRepository.findById("nonexistent")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.getCurrentUser("nonexistent"));
        assertEquals("User not found", ex.getMessage());
    }

    @Test
    void testValidateTokenValid() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("token@test.com");
        req.setPassword("password");
        req.setName("Token User");

        when(userRepository.existsByEmail("token@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("$2a$encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId("token-user-id");
            return u;
        });

        AuthResponse resp = authService.register(req);
        assertTrue(authService.validateToken(resp.getToken()));
    }

    @Test
    void testValidateTokenInvalid() {
        assertFalse(authService.validateToken("invalid.token.here"));
    }

    @Test
    void testGetUserIdFromToken() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("extract@test.com");
        req.setPassword("password");
        req.setName("Extract User");

        when(userRepository.existsByEmail("extract@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("$2a$encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId("extract-user-id");
            return u;
        });

        AuthResponse resp = authService.register(req);
        String extractedId = authService.getUserIdFromToken(resp.getToken());
        assertEquals("extract-user-id", extractedId);
    }

    @Test
    void testUpdateProfileName() {
        when(userRepository.findById("user-id-1")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        ProfileRequest req = new ProfileRequest();
        req.setName("Updated Name");

        User result = authService.updateProfile("user-id-1", req);

        assertEquals("Updated Name", result.getName());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testUpdateProfileEmail() {
        when(userRepository.findById("user-id-1")).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        ProfileRequest req = new ProfileRequest();
        req.setEmail("new@test.com");

        User result = authService.updateProfile("user-id-1", req);

        assertEquals("new@test.com", result.getEmail());
    }

    @Test
    void testUpdateProfileEmailAlreadyInUse() {
        when(userRepository.findById("user-id-1")).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail("taken@test.com")).thenReturn(true);

        ProfileRequest req = new ProfileRequest();
        req.setEmail("taken@test.com");

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.updateProfile("user-id-1", req));
        assertEquals("Email already in use by another account", ex.getMessage());
    }

    @Test
    void testUpdateProfileSameEmail() {
        when(userRepository.findById("user-id-1")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        ProfileRequest req = new ProfileRequest();
        req.setEmail("test@test.com");

        User result = authService.updateProfile("user-id-1", req);
        assertNotNull(result);
    }

    @Test
    void testUpdateProfilePassword() {
        when(userRepository.findById("user-id-1")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("newpass")).thenReturn("$2a$newencoded");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        ProfileRequest req = new ProfileRequest();
        req.setPassword("newpass");

        User result = authService.updateProfile("user-id-1", req);
        assertEquals("$2a$newencoded", result.getPassword());
    }

    @Test
    void testUpdateProfileEmptyPassword() {
        when(userRepository.findById("user-id-1")).thenReturn(Optional.of(testUser));

        ProfileRequest req = new ProfileRequest();
        req.setPassword("");

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.updateProfile("user-id-1", req));
        assertEquals("Password cannot be empty", ex.getMessage());
    }

    @Test
    void testUpdateProfileUserNotFound() {
        when(userRepository.findById("nonexistent")).thenReturn(Optional.empty());

        ProfileRequest req = new ProfileRequest();
        req.setName("New Name");

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.updateProfile("nonexistent", req));
        assertEquals("User not found", ex.getMessage());
    }

    @Test
    void testRegisterReturnsTokenWithClaims() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("claims@test.com");
        req.setPassword("password");
        req.setName("Claims User");

        when(userRepository.existsByEmail("claims@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("$2a$encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId("claims-user-id");
            return u;
        });

        AuthResponse resp = authService.register(req);
        String userId = authService.getUserIdFromToken(resp.getToken());
        assertEquals("claims-user-id", userId);
    }

    @Test
    void testLoginReturnsTokenWithClaims() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password", "$2a$encoded")).thenReturn(true);

        LoginRequest loginReq = new LoginRequest();
        loginReq.setEmail("test@test.com");
        loginReq.setPassword("password");
        AuthResponse resp = authService.login(loginReq);

        String userId = authService.getUserIdFromToken(resp.getToken());
        assertEquals("user-id-1", userId);
    }

    @Test
    void testGetUserIdFromTokenInvalid() {
        assertThrows(Exception.class, () -> authService.getUserIdFromToken("invalid.token"));
    }

    @Test
    void testValidateTokenExpired() {
        assertFalse(authService.validateToken("expired.invalid.token"));
    }
}
