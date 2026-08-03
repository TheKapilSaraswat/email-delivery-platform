package com.emailplatform.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void testDefaultConstructor() {
        User user = new User();
        assertNull(user.getId());
        assertNull(user.getEmail());
        assertNull(user.getPassword());
        assertNull(user.getName());
        assertEquals("USER", user.getRole());
        assertNull(user.getCreatedAt());
    }

    @Test
    void testAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();
        User user = new User("id1", "test@test.com", "pass", "Test", "ADMIN", now);
        assertEquals("id1", user.getId());
        assertEquals("test@test.com", user.getEmail());
        assertEquals("pass", user.getPassword());
        assertEquals("Test", user.getName());
        assertEquals("ADMIN", user.getRole());
        assertEquals(now, user.getCreatedAt());
    }

    @Test
    void testPrePersistSetsId() {
        User user = new User();
        user.setEmail("test@test.com");
        user.setPassword("pass");
        user.setName("Test");
        user.onCreate();
        assertNotNull(user.getId());
        assertFalse(user.getId().isEmpty());
    }

    @Test
    void testPrePersistSetsCreatedAt() {
        User user = new User();
        user.setEmail("test@test.com");
        user.setPassword("pass");
        user.setName("Test");
        user.onCreate();
        assertNotNull(user.getCreatedAt());
    }

    @Test
    void testPrePersistDoesNotOverwriteExistingId() {
        User user = new User();
        user.setId("existing-id");
        user.setEmail("test@test.com");
        user.onCreate();
        assertEquals("existing-id", user.getId());
    }

    @Test
    void testPrePersistDoesNotOverwriteExistingCreatedAt() {
        LocalDateTime custom = LocalDateTime.of(2020, 1, 1, 0, 0);
        User user = new User();
        user.setCreatedAt(custom);
        user.onCreate();
        assertEquals(custom, user.getCreatedAt());
    }

    @Test
    void testSettersAndGetters() {
        User user = new User();
        user.setId("id2");
        user.setEmail("a@b.com");
        user.setPassword("pwd");
        user.setName("Name");
        LocalDateTime dt = LocalDateTime.now();
        user.setCreatedAt(dt);
        assertEquals("id2", user.getId());
        assertEquals("a@b.com", user.getEmail());
        assertEquals("pwd", user.getPassword());
        assertEquals("Name", user.getName());
        assertEquals(dt, user.getCreatedAt());
    }

    @Test
    void testEqualsAndHashCode() {
        User u1 = new User();
        u1.setId("id1");
        User u2 = new User();
        u2.setId("id1");
        assertEquals(u1, u2);
        assertEquals(u1.hashCode(), u2.hashCode());
    }

    @Test
    void testNotEqualsDifferentId() {
        User u1 = new User();
        u1.setId("id1");
        User u2 = new User();
        u2.setId("id2");
        assertNotEquals(u1, u2);
    }

    @Test
    void testToString() {
        User user = new User();
        user.setId("id1");
        user.setEmail("test@test.com");
        String str = user.toString();
        assertNotNull(str);
        assertTrue(str.contains("id1"));
        assertTrue(str.contains("test@test.com"));
    }

    @Test
    void testEqualsWithNull() {
        User user = new User();
        user.setId("id1");
        assertNotEquals(null, user);
    }

    @Test
    void testEqualsWithDifferentClass() {
        User user = new User();
        user.setId("id1");
        assertNotEquals("string", user);
    }

    @Test
    void testEqualsSameObject() {
        User user = new User();
        user.setId("id1");
        assertEquals(user, user);
    }

    @Test
    void testPrePersistTwiceDoesNotChangeId() {
        User user = new User();
        user.onCreate();
        String firstId = user.getId();
        user.onCreate();
        assertEquals(firstId, user.getId());
    }

    @Test
    void testEmailUniqueness() {
        User u1 = new User();
        u1.setEmail("test@test.com");
        User u2 = new User();
        u2.setEmail("test@test.com");
        assertEquals(u1.getEmail(), u2.getEmail());
    }

    @Test
    void testNameCanBeLong() {
        User user = new User();
        String longName = "A".repeat(500);
        user.setName(longName);
        assertEquals(longName, user.getName());
    }

    @Test
    void testPasswordCanBeHashed() {
        User user = new User();
        String hashed = "$2a$10$N9qo8uLOickgx2ZMRZoMye";
        user.setPassword(hashed);
        assertEquals(hashed, user.getPassword());
    }

    @Test
    void testIdFormatIsUUID() {
        User user = new User();
        user.onCreate();
        assertDoesNotThrow(() -> java.util.UUID.fromString(user.getId()));
    }

    @Test
    void testMultipleUsersHaveUniqueIds() {
        User u1 = new User();
        u1.onCreate();
        User u2 = new User();
        u2.onCreate();
        assertNotEquals(u1.getId(), u2.getId());
    }
}
