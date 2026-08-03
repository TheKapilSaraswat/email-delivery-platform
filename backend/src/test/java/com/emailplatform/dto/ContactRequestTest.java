package com.emailplatform.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class ContactRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidRequest() {
        ContactRequest req = new ContactRequest();
        req.setEmail("test@test.com");
        req.setFirstName("John");
        req.setLastName("Doe");
        req.setList("newsletter");
        assertTrue(validator.validate(req).isEmpty());
    }

    @Test
    void testNullEmail() {
        ContactRequest req = new ContactRequest();
        req.setFirstName("John");
        assertFalse(validator.validate(req).isEmpty());
    }

    @Test
    void testBlankEmail() {
        ContactRequest req = new ContactRequest();
        req.setEmail("");
        assertFalse(validator.validate(req).isEmpty());
    }

    @Test
    void testInvalidEmailFormat() {
        ContactRequest req = new ContactRequest();
        req.setEmail("bad");
        assertFalse(validator.validate(req).isEmpty());
    }

    @Test
    void testOptionalFieldsNull() {
        ContactRequest req = new ContactRequest();
        req.setEmail("test@test.com");
        assertTrue(validator.validate(req).isEmpty());
        assertNull(req.getFirstName());
        assertNull(req.getLastName());
        assertNull(req.getList());
        assertNull(req.getMetadata());
    }

    @Test
    void testSettersAndGetters() {
        ContactRequest req = new ContactRequest();
        req.setEmail("a@b.com");
        req.setFirstName("Alice");
        req.setLastName("Smith");
        req.setList("vip");
        req.setMetadata("{\"src\":\"web\"}");
        assertEquals("a@b.com", req.getEmail());
        assertEquals("Alice", req.getFirstName());
        assertEquals("Smith", req.getLastName());
        assertEquals("vip", req.getList());
        assertEquals("{\"src\":\"web\"}", req.getMetadata());
    }

    @Test
    void testEqualsAndHashCode() {
        ContactRequest r1 = new ContactRequest();
        r1.setEmail("a@b.com");
        ContactRequest r2 = new ContactRequest();
        r2.setEmail("a@b.com");
        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    void testNotEqualsDifferentEmail() {
        ContactRequest r1 = new ContactRequest();
        r1.setEmail("a@b.com");
        ContactRequest r2 = new ContactRequest();
        r2.setEmail("c@d.com");
        assertNotEquals(r1, r2);
    }

    @Test
    void testToString() {
        ContactRequest req = new ContactRequest();
        req.setEmail("test@test.com");
        assertNotNull(req.toString());
    }

    @Test
    void testAllFieldsNull() {
        ContactRequest req = new ContactRequest();
        assertEquals(1, validator.validate(req).size());
    }
}
