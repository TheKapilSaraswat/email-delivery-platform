package com.emailplatform.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class TemplateRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidRequest() {
        TemplateRequest req = new TemplateRequest();
        req.setName("Welcome");
        req.setSubject("Hello!");
        req.setBody("<p>Hi</p>");
        assertTrue(validator.validate(req).isEmpty());
    }

    @Test
    void testNullName() {
        TemplateRequest req = new TemplateRequest();
        req.setSubject("Subj");
        req.setBody("Body");
        assertFalse(validator.validate(req).isEmpty());
    }

    @Test
    void testBlankName() {
        TemplateRequest req = new TemplateRequest();
        req.setName("");
        req.setSubject("Subj");
        req.setBody("Body");
        assertFalse(validator.validate(req).isEmpty());
    }

    @Test
    void testNullSubject() {
        TemplateRequest req = new TemplateRequest();
        req.setName("Name");
        req.setBody("Body");
        assertFalse(validator.validate(req).isEmpty());
    }

    @Test
    void testBlankSubject() {
        TemplateRequest req = new TemplateRequest();
        req.setName("Name");
        req.setSubject("");
        req.setBody("Body");
        assertFalse(validator.validate(req).isEmpty());
    }

    @Test
    void testNullBody() {
        TemplateRequest req = new TemplateRequest();
        req.setName("Name");
        req.setSubject("Subj");
        assertFalse(validator.validate(req).isEmpty());
    }

    @Test
    void testBlankBody() {
        TemplateRequest req = new TemplateRequest();
        req.setName("Name");
        req.setSubject("Subj");
        req.setBody("");
        assertFalse(validator.validate(req).isEmpty());
    }

    @Test
    void testAllFieldsNull() {
        TemplateRequest req = new TemplateRequest();
        assertEquals(3, validator.validate(req).size());
    }

    @Test
    void testSettersAndGetters() {
        TemplateRequest req = new TemplateRequest();
        req.setName("Newsletter");
        req.setSubject("Subject {{name}}");
        req.setBody("<h1>Hello {{name}}</h1>");
        assertEquals("Newsletter", req.getName());
        assertEquals("Subject {{name}}", req.getSubject());
        assertEquals("<h1>Hello {{name}}</h1>", req.getBody());
    }

    @Test
    void testEqualsAndHashCode() {
        TemplateRequest r1 = new TemplateRequest();
        r1.setName("Test");
        r1.setSubject("Subj");
        r1.setBody("Body");
        TemplateRequest r2 = new TemplateRequest();
        r2.setName("Test");
        r2.setSubject("Subj");
        r2.setBody("Body");
        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    void testNotEqualsDifferentName() {
        TemplateRequest r1 = new TemplateRequest();
        r1.setName("Test1");
        TemplateRequest r2 = new TemplateRequest();
        r2.setName("Test2");
        assertNotEquals(r1, r2);
    }

    @Test
    void testToString() {
        TemplateRequest req = new TemplateRequest();
        req.setName("Test");
        assertNotNull(req.toString());
    }
}
