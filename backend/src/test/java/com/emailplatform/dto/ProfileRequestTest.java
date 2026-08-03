package com.emailplatform.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ProfileRequestTest {

    @Test
    void testDefaultConstructor() {
        ProfileRequest req = new ProfileRequest();
        assertNull(req.getName());
        assertNull(req.getEmail());
        assertNull(req.getPassword());
    }

    @Test
    void testSettersAndGetters() {
        ProfileRequest req = new ProfileRequest();
        req.setName("New Name");
        req.setEmail("new@test.com");
        req.setPassword("newpass");
        assertEquals("New Name", req.getName());
        assertEquals("new@test.com", req.getEmail());
        assertEquals("newpass", req.getPassword());
    }

    @Test
    void testOptionalFieldsCanBeNull() {
        ProfileRequest req = new ProfileRequest();
        req.setName(null);
        req.setEmail(null);
        req.setPassword(null);
        assertNull(req.getName());
        assertNull(req.getEmail());
        assertNull(req.getPassword());
    }

    @Test
    void testPartialUpdate() {
        ProfileRequest req = new ProfileRequest();
        req.setName("Only Name");
        assertEquals("Only Name", req.getName());
        assertNull(req.getEmail());
        assertNull(req.getPassword());
    }

    @Test
    void testEqualsAndHashCode() {
        ProfileRequest r1 = new ProfileRequest();
        r1.setName("Name");
        r1.setEmail("a@b.com");
        r1.setPassword("pass");
        ProfileRequest r2 = new ProfileRequest();
        r2.setName("Name");
        r2.setEmail("a@b.com");
        r2.setPassword("pass");
        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    void testNotEqualsDifferentName() {
        ProfileRequest r1 = new ProfileRequest();
        r1.setName("Name1");
        ProfileRequest r2 = new ProfileRequest();
        r2.setName("Name2");
        assertNotEquals(r1, r2);
    }

    @Test
    void testToString() {
        ProfileRequest req = new ProfileRequest();
        req.setName("Test");
        assertNotNull(req.toString());
    }

    @Test
    void testEqualsWithNull() {
        ProfileRequest req = new ProfileRequest();
        assertNotEquals(null, req);
    }

    @Test
    void testEqualsSameObject() {
        ProfileRequest req = new ProfileRequest();
        assertEquals(req, req);
    }

    @Test
    void testPasswordCanBeEmpty() {
        ProfileRequest req = new ProfileRequest();
        req.setPassword("");
        assertEquals("", req.getPassword());
    }

    @Test
    void testEmailValidation() {
        ProfileRequest req = new ProfileRequest();
        req.setEmail("valid@email.com");
        assertEquals("valid@email.com", req.getEmail());
    }
}
