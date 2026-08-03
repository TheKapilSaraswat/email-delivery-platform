package com.emailplatform.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class ApiKeyTest {

    @Test
    void testDefaultConstructor() {
        ApiKey k = new ApiKey();
        assertNull(k.getId());
        assertNull(k.getKeyValue());
        assertNull(k.getName());
        assertNull(k.getUserId());
        assertFalse(k.isActive());
        assertNull(k.getCreatedAt());
    }

    @Test
    void testAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();
        ApiKey k = new ApiKey("id1", "ep_abc123", "Test Key", "uid", true, now);
        assertEquals("id1", k.getId());
        assertEquals("ep_abc123", k.getKeyValue());
        assertEquals("Test Key", k.getName());
        assertEquals("uid", k.getUserId());
        assertTrue(k.isActive());
        assertEquals(now, k.getCreatedAt());
    }

    @Test
    void testPrePersistSetsIdAndCreatedAt() {
        ApiKey k = new ApiKey();
        k.setName("Key");
        k.setKeyValue("ep_test");
        k.onCreate();
        assertNotNull(k.getId());
        assertNotNull(k.getCreatedAt());
        assertTrue(k.isActive());
    }

    @Test
    void testPrePersistDoesNotOverwriteId() {
        ApiKey k = new ApiKey();
        k.setId("custom-id");
        k.onCreate();
        assertEquals("custom-id", k.getId());
    }

    @Test
    void testPrePersistDoesNotOverwriteCreatedAt() {
        LocalDateTime custom = LocalDateTime.of(2022, 8, 15, 9, 0);
        ApiKey k = new ApiKey();
        k.setCreatedAt(custom);
        k.onCreate();
        assertEquals(custom, k.getCreatedAt());
    }

    @Test
    void testPrePersistSetsActiveToTrue() {
        ApiKey k = new ApiKey();
        k.setActive(false);
        k.onCreate();
        assertTrue(k.isActive());
    }

    @Test
    void testSettersAndGetters() {
        ApiKey k = new ApiKey();
        k.setId("id2");
        k.setKeyValue("ep_xyz");
        k.setName("Production Key");
        k.setUserId("u1");
        k.setActive(true);
        LocalDateTime dt = LocalDateTime.now();
        k.setCreatedAt(dt);
        assertEquals("id2", k.getId());
        assertEquals("ep_xyz", k.getKeyValue());
        assertEquals("Production Key", k.getName());
        assertEquals("u1", k.getUserId());
        assertTrue(k.isActive());
        assertEquals(dt, k.getCreatedAt());
    }

    @Test
    void testEqualsAndHashCode() {
        ApiKey k1 = new ApiKey();
        k1.setId("id1");
        ApiKey k2 = new ApiKey();
        k2.setId("id1");
        assertEquals(k1, k2);
        assertEquals(k1.hashCode(), k2.hashCode());
    }

    @Test
    void testNotEqualsDifferentId() {
        ApiKey k1 = new ApiKey();
        k1.setId("id1");
        ApiKey k2 = new ApiKey();
        k2.setId("id2");
        assertNotEquals(k1, k2);
    }

    @Test
    void testToString() {
        ApiKey k = new ApiKey();
        k.setId("id1");
        k.setName("Key");
        String str = k.toString();
        assertNotNull(str);
        assertTrue(str.contains("id1"));
    }

    @Test
    void testEqualsWithNull() {
        ApiKey k = new ApiKey();
        assertNotEquals(null, k);
    }

    @Test
    void testEqualsSameObject() {
        ApiKey k = new ApiKey();
        k.setId("id1");
        assertEquals(k, k);
    }

    @Test
    void testEqualsDifferentClass() {
        ApiKey k = new ApiKey();
        assertNotEquals("string", k);
    }

    @Test
    void testMultipleKeysUniqueIds() {
        ApiKey k1 = new ApiKey();
        k1.onCreate();
        ApiKey k2 = new ApiKey();
        k2.onCreate();
        assertNotEquals(k1.getId(), k2.getId());
    }

    @Test
    void testIdFormatIsUUID() {
        ApiKey k = new ApiKey();
        k.onCreate();
        assertDoesNotThrow(() -> java.util.UUID.fromString(k.getId()));
    }

    @Test
    void testKeyValueHasPrefix() {
        ApiKey k = new ApiKey();
        k.setKeyValue("ep_" + java.util.UUID.randomUUID().toString().replace("-", ""));
        assertTrue(k.getKeyValue().startsWith("ep_"));
    }

    @Test
    void testActiveCanBeToggled() {
        ApiKey k = new ApiKey();
        k.setActive(true);
        assertTrue(k.isActive());
        k.setActive(false);
        assertFalse(k.isActive());
        k.setActive(true);
        assertTrue(k.isActive());
    }

    @Test
    void testNameCanHaveSpecialChars() {
        ApiKey k = new ApiKey();
        k.setName("Production & Staging [v2]");
        assertEquals("Production & Staging [v2]", k.getName());
    }
}
