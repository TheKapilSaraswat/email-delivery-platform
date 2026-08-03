package com.emailplatform.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EmailSendRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidRequest() {
        EmailSendRequest req = new EmailSendRequest();
        req.setTo("test@test.com");
        req.setSubject("Hello");
        req.setBody("<p>Body</p>");
        assertTrue(validator.validate(req).isEmpty());
    }

    @Test
    void testNullTo() {
        EmailSendRequest req = new EmailSendRequest();
        req.setSubject("Subj");
        req.setBody("Body");
        assertFalse(validator.validate(req).isEmpty());
    }

    @Test
    void testBlankTo() {
        EmailSendRequest req = new EmailSendRequest();
        req.setTo("");
        req.setSubject("Subj");
        req.setBody("Body");
        assertFalse(validator.validate(req).isEmpty());
    }

    @Test
    void testInvalidToEmail() {
        EmailSendRequest req = new EmailSendRequest();
        req.setTo("bad-email");
        req.setSubject("Subj");
        req.setBody("Body");
        assertFalse(validator.validate(req).isEmpty());
    }

    @Test
    void testNullSubject() {
        EmailSendRequest req = new EmailSendRequest();
        req.setTo("test@test.com");
        req.setBody("Body");
        assertFalse(validator.validate(req).isEmpty());
    }

    @Test
    void testBlankSubject() {
        EmailSendRequest req = new EmailSendRequest();
        req.setTo("test@test.com");
        req.setSubject("");
        req.setBody("Body");
        assertFalse(validator.validate(req).isEmpty());
    }

    @Test
    void testNullBody() {
        EmailSendRequest req = new EmailSendRequest();
        req.setTo("test@test.com");
        req.setSubject("Subj");
        assertFalse(validator.validate(req).isEmpty());
    }

    @Test
    void testBlankBody() {
        EmailSendRequest req = new EmailSendRequest();
        req.setTo("test@test.com");
        req.setSubject("Subj");
        req.setBody("");
        assertFalse(validator.validate(req).isEmpty());
    }

    @Test
    void testAllFieldsNull() {
        EmailSendRequest req = new EmailSendRequest();
        assertEquals(3, validator.validate(req).size());
    }

    @Test
    void testSettersAndGetters() {
        EmailSendRequest req = new EmailSendRequest();
        req.setTo("a@b.com");
        req.setSubject("Test Subject");
        req.setBody("<p>HTML body</p>");
        assertEquals("a@b.com", req.getTo());
        assertEquals("Test Subject", req.getSubject());
        assertEquals("<p>HTML body</p>", req.getBody());
    }

    @Test
    void testEqualsAndHashCode() {
        EmailSendRequest r1 = new EmailSendRequest();
        r1.setTo("a@b.com");
        r1.setSubject("Subj");
        r1.setBody("Body");
        EmailSendRequest r2 = new EmailSendRequest();
        r2.setTo("a@b.com");
        r2.setSubject("Subj");
        r2.setBody("Body");
        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    void testNotEqualsDifferentTo() {
        EmailSendRequest r1 = new EmailSendRequest();
        r1.setTo("a@b.com");
        EmailSendRequest r2 = new EmailSendRequest();
        r2.setTo("c@d.com");
        assertNotEquals(r1, r2);
    }

    @Test
    void testToString() {
        EmailSendRequest req = new EmailSendRequest();
        req.setTo("test@test.com");
        assertNotNull(req.toString());
    }
}
