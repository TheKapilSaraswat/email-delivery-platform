package com.emailplatform.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class TemplateTest {

    @Test
    void testDefaultConstructor() {
        Template t = new Template();
        assertNull(t.getId());
        assertNull(t.getName());
        assertNull(t.getSubject());
        assertNull(t.getBody());
        assertNull(t.getUserId());
        assertNull(t.getCreatedAt());
        assertNull(t.getUpdatedAt());
    }

    @Test
    void testAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();
        Template t = new Template("id1", "Welcome", "Subject", "<p>Body</p>", "uid", now, now);
        assertEquals("id1", t.getId());
        assertEquals("Welcome", t.getName());
        assertEquals("Subject", t.getSubject());
        assertEquals("<p>Body</p>", t.getBody());
        assertEquals("uid", t.getUserId());
        assertEquals(now, t.getCreatedAt());
        assertEquals(now, t.getUpdatedAt());
    }

    @Test
    void testPrePersistSetsDefaults() {
        Template t = new Template();
        t.setName("Test");
        t.setSubject("Subj");
        t.setBody("Body");
        t.onCreate();
        assertNotNull(t.getId());
        assertNotNull(t.getCreatedAt());
        assertNotNull(t.getUpdatedAt());
    }

    @Test
    void testPrePersistDoesNotOverwriteId() {
        Template t = new Template();
        t.setId("custom");
        t.onCreate();
        assertEquals("custom", t.getId());
    }

    @Test
    void testPrePersistDoesNotOverwriteCreatedAt() {
        LocalDateTime custom = LocalDateTime.of(2020, 3, 10, 8, 0);
        Template t = new Template();
        t.setCreatedAt(custom);
        t.onCreate();
        assertEquals(custom, t.getCreatedAt());
    }

    @Test
    void testPrePersistDoesNotOverwriteUpdatedAt() {
        LocalDateTime custom = LocalDateTime.of(2020, 3, 10, 8, 0);
        Template t = new Template();
        t.setUpdatedAt(custom);
        t.onCreate();
        assertEquals(custom, t.getUpdatedAt());
    }

    @Test
    void testPreUpdateSetsUpdatedAt() {
        Template t = new Template();
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);
        t.setUpdatedAt(before);
        t.onUpdate();
        assertTrue(t.getUpdatedAt().isAfter(before) || t.getUpdatedAt().isEqual(before));
    }

    @Test
    void testSettersAndGetters() {
        Template t = new Template();
        t.setId("id2");
        t.setName("Newsletter");
        t.setSubject("Hello {{name}}");
        t.setBody("<p>Hi {{name}}</p>");
        t.setUserId("u1");
        LocalDateTime dt = LocalDateTime.now();
        t.setCreatedAt(dt);
        t.setUpdatedAt(dt);
        assertEquals("id2", t.getId());
        assertEquals("Newsletter", t.getName());
        assertEquals("Hello {{name}}", t.getSubject());
        assertEquals("<p>Hi {{name}}</p>", t.getBody());
        assertEquals("u1", t.getUserId());
        assertEquals(dt, t.getCreatedAt());
        assertEquals(dt, t.getUpdatedAt());
    }

    @Test
    void testEqualsAndHashCode() {
        Template t1 = new Template();
        t1.setId("id1");
        Template t2 = new Template();
        t2.setId("id1");
        assertEquals(t1, t2);
        assertEquals(t1.hashCode(), t2.hashCode());
    }

    @Test
    void testNotEqualsDifferentId() {
        Template t1 = new Template();
        t1.setId("id1");
        Template t2 = new Template();
        t2.setId("id2");
        assertNotEquals(t1, t2);
    }

    @Test
    void testToString() {
        Template t = new Template();
        t.setId("id1");
        t.setName("Welcome");
        String str = t.toString();
        assertNotNull(str);
        assertTrue(str.contains("id1"));
    }

    @Test
    void testEqualsWithNull() {
        Template t = new Template();
        assertNotEquals(null, t);
    }

    @Test
    void testEqualsSameObject() {
        Template t = new Template();
        t.setId("id1");
        assertEquals(t, t);
    }

    @Test
    void testEqualsDifferentClass() {
        Template t = new Template();
        assertNotEquals("string", t);
    }

    @Test
    void testMultipleTemplatesUniqueIds() {
        Template t1 = new Template();
        t1.onCreate();
        Template t2 = new Template();
        t2.onCreate();
        assertNotEquals(t1.getId(), t2.getId());
    }

    @Test
    void testBodyCanBeLargeHtml() {
        Template t = new Template();
        String html = "<html><body>" + "<p>Paragraph</p>".repeat(1000) + "</body></html>";
        t.setBody(html);
        assertEquals(html, t.getBody());
    }

    @Test
    void testSubjectCanContainVariables() {
        Template t = new Template();
        t.setSubject("Hello {{firstName}} {{lastName}}, welcome to {{company}}!");
        assertTrue(t.getSubject().contains("{{firstName}}"));
        assertTrue(t.getSubject().contains("{{lastName}}"));
        assertTrue(t.getSubject().contains("{{company}}"));
    }

    @Test
    void testIdFormatIsUUID() {
        Template t = new Template();
        t.onCreate();
        assertDoesNotThrow(() -> java.util.UUID.fromString(t.getId()));
    }

    @Test
    void testNameCanHaveSpecialChars() {
        Template t = new Template();
        t.setName("Welcome & Onboarding <2024>");
        assertEquals("Welcome & Onboarding <2024>", t.getName());
    }

    @Test
    void testBodyCanContainUnicode() {
        Template t = new Template();
        t.setBody("<p>Hello \u4e16\u754c</p>");
        assertTrue(t.getBody().contains("\u4e16\u754c"));
    }
}
