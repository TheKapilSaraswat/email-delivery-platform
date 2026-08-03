package com.emailplatform.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ApiKeyRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidRequest() {
        ApiKeyRequest req = new ApiKeyRequest();
        req.setName("Test Key");
        assertTrue(validator.validate(req).isEmpty());
    }

    @Test
    void testNullName() {
        ApiKeyRequest req = new ApiKeyRequest();
        assertFalse(validator.validate(req).isEmpty());
    }

    @Test
    void testBlankName() {
        ApiKeyRequest req = new ApiKeyRequest();
        req.setName("");
        assertFalse(validator.validate(req).isEmpty());
    }

    @Test
    void testSettersAndGetters() {
        ApiKeyRequest req = new ApiKeyRequest();
        req.setName("Production Key");
        assertEquals("Production Key", req.getName());
    }

    @Test
    void testEqualsAndHashCode() {
        ApiKeyRequest r1 = new ApiKeyRequest();
        r1.setName("Key1");
        ApiKeyRequest r2 = new ApiKeyRequest();
        r2.setName("Key1");
        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    void testNotEqualsDifferentName() {
        ApiKeyRequest r1 = new ApiKeyRequest();
        r1.setName("Key1");
        ApiKeyRequest r2 = new ApiKeyRequest();
        r2.setName("Key2");
        assertNotEquals(r1, r2);
    }

    @Test
    void testToString() {
        ApiKeyRequest req = new ApiKeyRequest();
        req.setName("Key");
        assertNotNull(req.toString());
    }

    @Test
    void testEqualsWithNull() {
        ApiKeyRequest req = new ApiKeyRequest();
        assertNotEquals(null, req);
    }

    @Test
    void testEqualsSameObject() {
        ApiKeyRequest req = new ApiKeyRequest();
        req.setName("Key");
        assertEquals(req, req);
    }
}
