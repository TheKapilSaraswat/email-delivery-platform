package com.emailplatform.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class RegisterRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidRequest() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("test@test.com");
        req.setPassword("password");
        req.setName("Test User");
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(req);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testNullEmail() {
        RegisterRequest req = new RegisterRequest();
        req.setPassword("password");
        req.setName("Test");
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(req);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testBlankEmail() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("");
        req.setPassword("password");
        req.setName("Test");
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(req);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testInvalidEmailFormat() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("not-an-email");
        req.setPassword("password");
        req.setName("Test");
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(req);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testNullPassword() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("test@test.com");
        req.setName("Test");
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(req);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testBlankPassword() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("test@test.com");
        req.setPassword("");
        req.setName("Test");
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(req);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testNullName() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("test@test.com");
        req.setPassword("password");
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(req);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testBlankName() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("test@test.com");
        req.setPassword("password");
        req.setName("");
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(req);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testSettersAndGetters() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("a@b.com");
        req.setPassword("pwd");
        req.setName("Name");
        assertEquals("a@b.com", req.getEmail());
        assertEquals("pwd", req.getPassword());
        assertEquals("Name", req.getName());
    }

    @Test
    void testAllFieldsNull() {
        RegisterRequest req = new RegisterRequest();
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(req);
        assertEquals(3, violations.size());
    }

    @Test
    void testEmailWithSubdomain() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("user@mail.test.com");
        req.setPassword("password");
        req.setName("Name");
        assertTrue(validator.validate(req).isEmpty());
    }

    @Test
    void testEmailWithPlus() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("user+tag@test.com");
        req.setPassword("password");
        req.setName("Name");
        assertTrue(validator.validate(req).isEmpty());
    }

    @Test
    void testLongEmail() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("a".repeat(50) + "@test.com");
        req.setPassword("password");
        req.setName("Name");
        assertTrue(validator.validate(req).isEmpty());
    }

    @Test
    void testSpecialCharsInName() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("test@test.com");
        req.setPassword("password");
        req.setName("O'Brien-Smith Jr.");
        assertTrue(validator.validate(req).isEmpty());
    }

    @Test
    void testUnicodeName() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("test@test.com");
        req.setPassword("password");
        req.setName("\u00c9t\u00e9 \u00dalo\u00fc\u00efs");
        assertTrue(validator.validate(req).isEmpty());
    }
}
