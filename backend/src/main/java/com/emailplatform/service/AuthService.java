package com.emailplatform.service;

import com.emailplatform.dto.AuthResponse;
import com.emailplatform.dto.LoginRequest;
import com.emailplatform.dto.ProfileRequest;
import com.emailplatform.dto.RegisterRequest;
import com.emailplatform.model.User;
import com.emailplatform.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecretKey secretKey;
    private final long expiration;
    private final String adminEmail;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       @Value("${jwt.secret}") String secret,
                       @Value("${jwt.expiration}") long expiration,
                       @Value("${app.admin-email:${spring.mail.username:}}") String adminEmail) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
        this.adminEmail = adminEmail == null ? "" : adminEmail.trim().toLowerCase();
    }

    public AuthResponse register(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("An account with this email already exists. Please login instead.");
        }
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName().trim());
        if (!adminEmail.isEmpty() && email.equals(adminEmail)) {
            user.setRole("ADMIN");
        }
        user = userRepository.save(user);
        String token = generateToken(user);
        return new AuthResponse(token, user.getId(), user.getEmail(), user.getName());
    }

    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }
        String token = generateToken(user);
        return new AuthResponse(token, user.getId(), user.getEmail(), user.getName());
    }

    public User getCurrentUser(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private String generateToken(User user) {
        Date now = new Date();
        return Jwts.builder()
                .subject(user.getId())
                .claim("email", user.getEmail())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expiration))
                .signWith(secretKey)
                .compact();
    }

    public String getUserIdFromToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public User updateProfile(String userId, ProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (request.getName() != null) {
            String trimmedName = request.getName().trim();
            if (trimmedName.isEmpty()) {
                throw new RuntimeException("Name cannot be empty");
            }
            user.setName(trimmedName);
        }
        if (request.getEmail() != null) {
            String trimmedEmail = request.getEmail().trim();
            if (!user.getEmail().equals(trimmedEmail) && userRepository.existsByEmail(trimmedEmail)) {
                throw new RuntimeException("Email already in use by another account");
            }
            user.setEmail(trimmedEmail);
        }
        if (request.getPassword() != null) {
            String trimmedPassword = request.getPassword().trim();
            if (trimmedPassword.isEmpty()) {
                throw new RuntimeException("Password cannot be empty");
            }
            if (trimmedPassword.length() < 6) {
                throw new RuntimeException("Password must be at least 6 characters");
            }
            user.setPassword(passwordEncoder.encode(trimmedPassword));
        }
        return userRepository.save(user);
    }
}
