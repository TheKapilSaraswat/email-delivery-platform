package com.emailplatform.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class LoginRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidRequest() {
        LoginRequest req = new LoginRequest();
        req.setEmail("test@test.com");
        req.setPassword("password");
        assertTrue(validator.validate(req).isEmpty());
    }

    @Test
    void testNullEmail() {
        LoginRequest req = new LoginRequest();
        req.setPassword("password");
        assertFalse(validator.validate(req).isEmpty());
    }

    @Test
    void testBlankEmail() {
        LoginRequest req = new LoginRequest();
        req.setEmail("");
        req.setPassword("password");
        assertFalse(validator.validate(req).isEmpty());
    }

    @Test
    void testInvalidEmailFormat() {
        LoginRequest req = new LoginRequest();
        req.setEmail("bad-email");
        req.setPassword("password");
        assertFalse(validator.validate(req).isEmpty());
    }

    @Test
    void testNullPassword() {
        LoginRequest req = new LoginRequest();
        req.setEmail("test@test.com");
        assertFalse(validator.validate(req).isEmpty());
    }

    @Test
    void testBlankPassword() {
        LoginRequest req = new LoginRequest();
        req.setEmail("test@test.com");
        req.setPassword("");
        assertFalse(validator.validate(req).isEmpty());
    }

    @Test
    void testSettersAndGetters() {
        LoginRequest req = new LoginRequest();
        req.setEmail("a@b.com");
        req.setPassword("pwd");
        assertEquals("a@b.com", req.getEmail());
        assertEquals("pwd", req.getPassword());
    }

    @Test
    void testAllFieldsNull() {
        LoginRequest req = new LoginRequest();
        assertEquals(2, validator.validate(req).size());
    }

    @Test
    void testEmailWithSubdomain() {
        LoginRequest req = new LoginRequest();
        req.setEmail("user@mail.company.com");
        req.setPassword("pass");
        assertTrue(validator.validate(req).isEmpty());
    }

    @Test
    void testLongPassword() {
        LoginRequest req = new LoginRequest();
        req.setEmail("test@test.com");
        req.setPassword("a".repeat(256));
        assertTrue(validator.validate(req).isEmpty());
    }

    @Test
    void testSpecialCharsPassword() {
        LoginRequest req = new LoginRequest();
        req.setEmail("test@test.com");
        req.setPassword("P@$$w0rd!#%^&*()");
        assertTrue(validator.validate(req).isEmpty());
    }

    @Test
    void testUnicodePassword() {
        LoginRequest req = new LoginRequest();
        req.setEmail("test@test.com");
        req.setPassword("\u00e4\u00f6\u00fc\u00df");
        assertTrue(validator.validate(req).isEmpty());
    }
}
