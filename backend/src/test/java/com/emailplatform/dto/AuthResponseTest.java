package com.emailplatform.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AuthResponseTest {

    @Test
    void testConstructor() {
        AuthResponse resp = new AuthResponse("token123", "userId1", "test@test.com", "Test User");
        assertEquals("token123", resp.getToken());
        assertEquals("userId1", resp.getUserId());
        assertEquals("test@test.com", resp.getEmail());
        assertEquals("Test User", resp.getName());
    }

    @Test
    void testSettersAndGetters() {
        AuthResponse resp = new AuthResponse(null, null, null, null);
        resp.setToken("tok");
        resp.setUserId("uid");
        resp.setEmail("a@b.com");
        resp.setName("Name");
        assertEquals("tok", resp.getToken());
        assertEquals("uid", resp.getUserId());
        assertEquals("a@b.com", resp.getEmail());
        assertEquals("Name", resp.getName());
    }

    @Test
    void testNullValues() {
        AuthResponse resp = new AuthResponse(null, null, null, null);
        assertNull(resp.getToken());
        assertNull(resp.getUserId());
        assertNull(resp.getEmail());
        assertNull(resp.getName());
    }

    @Test
    void testEqualsAndHashCode() {
        AuthResponse r1 = new AuthResponse("tok", "uid", "a@b.com", "Name");
        AuthResponse r2 = new AuthResponse("tok", "uid", "a@b.com", "Name");
        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    void testNotEqualsDifferentToken() {
        AuthResponse r1 = new AuthResponse("tok1", "uid", "a@b.com", "Name");
        AuthResponse r2 = new AuthResponse("tok2", "uid", "a@b.com", "Name");
        assertNotEquals(r1, r2);
    }

    @Test
    void testNotEqualsDifferentUserId() {
        AuthResponse r1 = new AuthResponse("tok", "uid1", "a@b.com", "Name");
        AuthResponse r2 = new AuthResponse("tok", "uid2", "a@b.com", "Name");
        assertNotEquals(r1, r2);
    }

    @Test
    void testToString() {
        AuthResponse resp = new AuthResponse("tok", "uid", "a@b.com", "Name");
        String str = resp.toString();
        assertNotNull(str);
        assertTrue(str.contains("tok"));
    }

    @Test
    void testEqualsWithNull() {
        AuthResponse resp = new AuthResponse("tok", "uid", "a@b.com", "Name");
        assertNotEquals(null, resp);
    }

    @Test
    void testEqualsSameObject() {
        AuthResponse resp = new AuthResponse("tok", "uid", "a@b.com", "Name");
        assertEquals(resp, resp);
    }

    @Test
    void testEqualsDifferentClass() {
        AuthResponse resp = new AuthResponse("tok", "uid", "a@b.com", "Name");
        assertNotEquals("string", resp);
    }
}
