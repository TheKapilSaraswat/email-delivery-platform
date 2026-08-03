package com.emailplatform.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class AnalyticsEventTest {

    @Test
    void testDefaultConstructor() {
        AnalyticsEvent e = new AnalyticsEvent();
        assertNull(e.getId());
        assertNull(e.getCampaignId());
        assertNull(e.getContactId());
        assertNull(e.getEventType());
        assertNull(e.getUrl());
        assertNull(e.getTimestamp());
    }

    @Test
    void testAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();
        AnalyticsEvent e = new AnalyticsEvent("id1", "cid", "ctid", "open", "http://url", now);
        assertEquals("id1", e.getId());
        assertEquals("cid", e.getCampaignId());
        assertEquals("ctid", e.getContactId());
        assertEquals("open", e.getEventType());
        assertEquals("http://url", e.getUrl());
        assertEquals(now, e.getTimestamp());
    }

    @Test
    void testPrePersistSetsIdAndTimestamp() {
        AnalyticsEvent e = new AnalyticsEvent();
        e.setCampaignId("c1");
        e.setContactId("ct1");
        e.setEventType("open");
        e.onCreate();
        assertNotNull(e.getId());
        assertNotNull(e.getTimestamp());
    }

    @Test
    void testPrePersistDoesNotOverwriteId() {
        AnalyticsEvent e = new AnalyticsEvent();
        e.setId("custom-id");
        e.onCreate();
        assertEquals("custom-id", e.getId());
    }

    @Test
    void testPrePersistDoesNotOverwriteTimestamp() {
        LocalDateTime custom = LocalDateTime.of(2023, 1, 1, 0, 0);
        AnalyticsEvent e = new AnalyticsEvent();
        e.setTimestamp(custom);
        e.onCreate();
        assertEquals(custom, e.getTimestamp());
    }

    @Test
    void testSettersAndGetters() {
        AnalyticsEvent e = new AnalyticsEvent();
        e.setId("id2");
        e.setCampaignId("c1");
        e.setContactId("ct1");
        e.setEventType("click");
        e.setUrl("http://example.com");
        LocalDateTime dt = LocalDateTime.now();
        e.setTimestamp(dt);
        assertEquals("id2", e.getId());
        assertEquals("c1", e.getCampaignId());
        assertEquals("ct1", e.getContactId());
        assertEquals("click", e.getEventType());
        assertEquals("http://example.com", e.getUrl());
        assertEquals(dt, e.getTimestamp());
    }

    @Test
    void testEqualsAndHashCode() {
        AnalyticsEvent e1 = new AnalyticsEvent();
        e1.setId("id1");
        AnalyticsEvent e2 = new AnalyticsEvent();
        e2.setId("id1");
        assertEquals(e1, e2);
        assertEquals(e1.hashCode(), e2.hashCode());
    }

    @Test
    void testNotEqualsDifferentId() {
        AnalyticsEvent e1 = new AnalyticsEvent();
        e1.setId("id1");
        AnalyticsEvent e2 = new AnalyticsEvent();
        e2.setId("id2");
        assertNotEquals(e1, e2);
    }

    @Test
    void testToString() {
        AnalyticsEvent e = new AnalyticsEvent();
        e.setId("id1");
        e.setEventType("open");
        String str = e.toString();
        assertNotNull(str);
        assertTrue(str.contains("id1"));
    }

    @Test
    void testEqualsWithNull() {
        AnalyticsEvent e = new AnalyticsEvent();
        assertNotEquals(null, e);
    }

    @Test
    void testEqualsSameObject() {
        AnalyticsEvent e = new AnalyticsEvent();
        e.setId("id1");
        assertEquals(e, e);
    }

    @Test
    void testEqualsDifferentClass() {
        AnalyticsEvent e = new AnalyticsEvent();
        assertNotEquals("string", e);
    }

    @Test
    void testMultipleEventsUniqueIds() {
        AnalyticsEvent e1 = new AnalyticsEvent();
        e1.onCreate();
        AnalyticsEvent e2 = new AnalyticsEvent();
        e2.onCreate();
        assertNotEquals(e1.getId(), e2.getId());
    }

    @Test
    void testIdFormatIsUUID() {
        AnalyticsEvent e = new AnalyticsEvent();
        e.onCreate();
        assertDoesNotThrow(() -> java.util.UUID.fromString(e.getId()));
    }

    @Test
    void testEventTypeValues() {
        AnalyticsEvent e = new AnalyticsEvent();
        e.setEventType("open");
        assertEquals("open", e.getEventType());
        e.setEventType("click");
        assertEquals("click", e.getEventType());
        e.setEventType("bounce");
        assertEquals("bounce", e.getEventType());
        e.setEventType("sent");
        assertEquals("sent", e.getEventType());
    }

    @Test
    void testUrlCanBeLong() {
        AnalyticsEvent e = new AnalyticsEvent();
        String longUrl = "http://example.com/" + "a".repeat(2000);
        e.setUrl(longUrl);
        assertEquals(longUrl, e.getUrl());
    }

    @Test
    void testUrlCanBeNull() {
        AnalyticsEvent e = new AnalyticsEvent();
        e.setUrl(null);
        assertNull(e.getUrl());
    }

    @Test
    void testContactIdCanBeSet() {
        AnalyticsEvent e = new AnalyticsEvent();
        e.setContactId("contact-123");
        assertEquals("contact-123", e.getContactId());
    }

    @Test
    void testCampaignIdCanBeSet() {
        AnalyticsEvent e = new AnalyticsEvent();
        e.setCampaignId("campaign-456");
        assertEquals("campaign-456", e.getCampaignId());
    }
}
