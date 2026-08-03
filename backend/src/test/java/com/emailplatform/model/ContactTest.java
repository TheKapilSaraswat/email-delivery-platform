package com.emailplatform.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class ContactTest {

    @Test
    void testDefaultConstructor() {
        Contact c = new Contact();
        assertNull(c.getId());
        assertNull(c.getEmail());
        assertNull(c.getFirstName());
        assertNull(c.getLastName());
        assertNull(c.getList());
        assertNull(c.getMetadata());
        assertNull(c.getUserId());
        assertNull(c.getCreatedAt());
    }

    @Test
    void testAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();
        Contact c = new Contact("id1", "john@test.com", "John", "Doe", "list", "meta", "uid", now);
        assertEquals("id1", c.getId());
        assertEquals("john@test.com", c.getEmail());
        assertEquals("John", c.getFirstName());
        assertEquals("Doe", c.getLastName());
        assertEquals("list", c.getList());
        assertEquals("meta", c.getMetadata());
        assertEquals("uid", c.getUserId());
        assertEquals(now, c.getCreatedAt());
    }

    @Test
    void testPrePersistSetsIdAndCreatedAt() {
        Contact c = new Contact();
        c.setEmail("test@test.com");
        c.onCreate();
        assertNotNull(c.getId());
        assertNotNull(c.getCreatedAt());
    }

    @Test
    void testPrePersistDoesNotOverwriteId() {
        Contact c = new Contact();
        c.setId("custom-id");
        c.onCreate();
        assertEquals("custom-id", c.getId());
    }

    @Test
    void testPrePersistDoesNotOverwriteCreatedAt() {
        LocalDateTime custom = LocalDateTime.of(2021, 5, 20, 12, 0);
        Contact c = new Contact();
        c.setCreatedAt(custom);
        c.onCreate();
        assertEquals(custom, c.getCreatedAt());
    }

    @Test
    void testSettersAndGetters() {
        Contact c = new Contact();
        c.setId("id2");
        c.setEmail("a@b.com");
        c.setFirstName("Alice");
        c.setLastName("Smith");
        c.setList("newsletter");
        c.setMetadata("{\"key\":\"value\"}");
        c.setUserId("u1");
        LocalDateTime dt = LocalDateTime.now();
        c.setCreatedAt(dt);
        assertEquals("id2", c.getId());
        assertEquals("a@b.com", c.getEmail());
        assertEquals("Alice", c.getFirstName());
        assertEquals("Smith", c.getLastName());
        assertEquals("newsletter", c.getList());
        assertEquals("{\"key\":\"value\"}", c.getMetadata());
        assertEquals("u1", c.getUserId());
        assertEquals(dt, c.getCreatedAt());
    }

    @Test
    void testEqualsAndHashCode() {
        Contact c1 = new Contact();
        c1.setId("id1");
        Contact c2 = new Contact();
        c2.setId("id1");
        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());
    }

    @Test
    void testNotEqualsDifferentId() {
        Contact c1 = new Contact();
        c1.setId("id1");
        Contact c2 = new Contact();
        c2.setId("id2");
        assertNotEquals(c1, c2);
    }

    @Test
    void testToString() {
        Contact c = new Contact();
        c.setId("id1");
        c.setEmail("test@test.com");
        String str = c.toString();
        assertNotNull(str);
        assertTrue(str.contains("id1"));
    }

    @Test
    void testEqualsWithNull() {
        Contact c = new Contact();
        assertNotEquals(null, c);
    }

    @Test
    void testEqualsSameObject() {
        Contact c = new Contact();
        c.setId("id1");
        assertEquals(c, c);
    }

    @Test
    void testEqualsDifferentClass() {
        Contact c = new Contact();
        assertNotEquals("string", c);
    }

    @Test
    void testMultipleContactsUniqueIds() {
        Contact c1 = new Contact();
        c1.onCreate();
        Contact c2 = new Contact();
        c2.onCreate();
        assertNotEquals(c1.getId(), c2.getId());
    }

    @Test
    void testIdFormatIsUUID() {
        Contact c = new Contact();
        c.onCreate();
        assertDoesNotThrow(() -> java.util.UUID.fromString(c.getId()));
    }

    @Test
    void testEmailCanBeLong() {
        Contact c = new Contact();
        String longEmail = "a".repeat(100) + "@test.com";
        c.setEmail(longEmail);
        assertEquals(longEmail, c.getEmail());
    }

    @Test
    void testMetadataCanBeJson() {
        Contact c = new Contact();
        String json = "{\"source\":\"import\",\"tags\":[\"vip\",\"active\"]}";
        c.setMetadata(json);
        assertEquals(json, c.getMetadata());
    }

    @Test
    void testListCanBeDefault() {
        Contact c = new Contact();
        c.setList("default");
        assertEquals("default", c.getList());
    }

    @Test
    void testFirstNameAndLastNameCanBeNull() {
        Contact c = new Contact();
        c.setFirstName(null);
        c.setLastName(null);
        assertNull(c.getFirstName());
        assertNull(c.getLastName());
    }

    @Test
    void testFirstNameCanHaveSpaces() {
        Contact c = new Contact();
        c.setFirstName("Mary Jane");
        assertEquals("Mary Jane", c.getFirstName());
    }

    @Test
    void testLastNameCanHaveSpaces() {
        Contact c = new Contact();
        c.setLastName("Van Der Berg");
        assertEquals("Van Der Berg", c.getLastName());
    }
}
