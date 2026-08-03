package com.emailplatform.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class CampaignTest {

    @Test
    void testDefaultConstructor() {
        Campaign c = new Campaign();
        assertNull(c.getId());
        assertNull(c.getName());
        assertNull(c.getDescription());
        assertNull(c.getTemplateId());
        assertNull(c.getUserId());
        assertNull(c.getContactList());
        assertNull(c.getStatus());
        assertNull(c.getSentCount());
        assertNull(c.getOpenedCount());
        assertNull(c.getClickedCount());
        assertNull(c.getScheduledAt());
        assertNull(c.getSentAt());
        assertNull(c.getCreatedAt());
    }

    @Test
    void testAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();
        Campaign c = new Campaign("id1", "Test", "Desc", "tid", "uid", "list", "draft", 0, 0, 0, null, null, now);
        assertEquals("id1", c.getId());
        assertEquals("Test", c.getName());
        assertEquals("Desc", c.getDescription());
        assertEquals("tid", c.getTemplateId());
        assertEquals("uid", c.getUserId());
        assertEquals("list", c.getContactList());
        assertEquals("draft", c.getStatus());
        assertEquals(0, c.getSentCount());
        assertEquals(0, c.getOpenedCount());
        assertEquals(0, c.getClickedCount());
        assertNull(c.getScheduledAt());
        assertNull(c.getSentAt());
        assertEquals(now, c.getCreatedAt());
    }

    @Test
    void testPrePersistSetsDefaults() {
        Campaign c = new Campaign();
        c.setName("Test");
        c.onCreate();
        assertNotNull(c.getId());
        assertEquals("draft", c.getStatus());
        assertEquals(0, c.getSentCount());
        assertEquals(0, c.getOpenedCount());
        assertEquals(0, c.getClickedCount());
        assertNotNull(c.getCreatedAt());
    }

    @Test
    void testPrePersistDoesNotOverwriteId() {
        Campaign c = new Campaign();
        c.setId("custom-id");
        c.onCreate();
        assertEquals("custom-id", c.getId());
    }

    @Test
    void testPrePersistDoesNotOverwriteStatus() {
        Campaign c = new Campaign();
        c.setStatus("sending");
        c.onCreate();
        assertEquals("sending", c.getStatus());
    }

    @Test
    void testPrePersistDoesNotOverwriteCounters() {
        Campaign c = new Campaign();
        c.setSentCount(5);
        c.setOpenedCount(3);
        c.setClickedCount(1);
        c.onCreate();
        assertEquals(5, c.getSentCount());
        assertEquals(3, c.getOpenedCount());
        assertEquals(1, c.getClickedCount());
    }

    @Test
    void testPrePersistDoesNotOverwriteCreatedAt() {
        LocalDateTime custom = LocalDateTime.of(2020, 6, 15, 10, 0);
        Campaign c = new Campaign();
        c.setCreatedAt(custom);
        c.onCreate();
        assertEquals(custom, c.getCreatedAt());
    }

    @Test
    void testSettersAndGetters() {
        Campaign c = new Campaign();
        c.setId("id2");
        c.setName("New Campaign");
        c.setDescription("Description");
        c.setTemplateId("t1");
        c.setUserId("u1");
        c.setContactList("newsletter");
        c.setStatus("sent");
        c.setSentCount(100);
        c.setOpenedCount(50);
        c.setClickedCount(10);
        LocalDateTime dt = LocalDateTime.now();
        c.setScheduledAt(dt);
        c.setSentAt(dt);
        c.setCreatedAt(dt);
        assertEquals("id2", c.getId());
        assertEquals("New Campaign", c.getName());
        assertEquals("Description", c.getDescription());
        assertEquals("t1", c.getTemplateId());
        assertEquals("u1", c.getUserId());
        assertEquals("newsletter", c.getContactList());
        assertEquals("sent", c.getStatus());
        assertEquals(100, c.getSentCount());
        assertEquals(50, c.getOpenedCount());
        assertEquals(10, c.getClickedCount());
        assertEquals(dt, c.getScheduledAt());
        assertEquals(dt, c.getSentAt());
        assertEquals(dt, c.getCreatedAt());
    }

    @Test
    void testStatusTransitions() {
        Campaign c = new Campaign();
        c.onCreate();
        assertEquals("draft", c.getStatus());
        c.setStatus("scheduled");
        assertEquals("scheduled", c.getStatus());
        c.setStatus("sending");
        assertEquals("sending", c.getStatus());
        c.setStatus("sent");
        assertEquals("sent", c.getStatus());
    }

    @Test
    void testEqualsAndHashCode() {
        Campaign c1 = new Campaign();
        c1.setId("id1");
        Campaign c2 = new Campaign();
        c2.setId("id1");
        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());
    }

    @Test
    void testNotEqualsDifferentId() {
        Campaign c1 = new Campaign();
        c1.setId("id1");
        Campaign c2 = new Campaign();
        c2.setId("id2");
        assertNotEquals(c1, c2);
    }

    @Test
    void testToString() {
        Campaign c = new Campaign();
        c.setId("id1");
        c.setName("Test");
        String str = c.toString();
        assertNotNull(str);
        assertTrue(str.contains("id1"));
        assertTrue(str.contains("Test"));
    }

    @Test
    void testEqualsWithNull() {
        Campaign c = new Campaign();
        assertNotEquals(null, c);
    }

    @Test
    void testEqualsSameObject() {
        Campaign c = new Campaign();
        c.setId("id1");
        assertEquals(c, c);
    }

    @Test
    void testEqualsDifferentClass() {
        Campaign c = new Campaign();
        c.setId("id1");
        assertNotEquals("string", c);
    }

    @Test
    void testMultipleCampaignsUniqueIds() {
        Campaign c1 = new Campaign();
        c1.onCreate();
        Campaign c2 = new Campaign();
        c2.onCreate();
        assertNotEquals(c1.getId(), c2.getId());
    }

    @Test
    void testDescriptionCanBeLong() {
        Campaign c = new Campaign();
        String longDesc = "A".repeat(5000);
        c.setDescription(longDesc);
        assertEquals(longDesc, c.getDescription());
    }

    @Test
    void testContactListCanBeEmpty() {
        Campaign c = new Campaign();
        c.setContactList("");
        assertEquals("", c.getContactList());
    }

    @Test
    void testContactListBeNull() {
        Campaign c = new Campaign();
        c.setContactList(null);
        assertNull(c.getContactList());
    }

    @Test
    void testSentCountCanBeLarge() {
        Campaign c = new Campaign();
        c.setSentCount(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, c.getSentCount());
    }

    @Test
    void testIdFormatIsUUID() {
        Campaign c = new Campaign();
        c.onCreate();
        assertDoesNotThrow(() -> java.util.UUID.fromString(c.getId()));
    }

    @Test
    void testScheduledAtCanBeSet() {
        Campaign c = new Campaign();
        LocalDateTime sched = LocalDateTime.of(2025, 12, 25, 10, 0);
        c.setScheduledAt(sched);
        assertEquals(sched, c.getScheduledAt());
    }

    @Test
    void testSentAtCanBeSet() {
        Campaign c = new Campaign();
        LocalDateTime sent = LocalDateTime.of(2025, 1, 15, 14, 30);
        c.setSentAt(sent);
        assertEquals(sent, c.getSentAt());
    }
}
