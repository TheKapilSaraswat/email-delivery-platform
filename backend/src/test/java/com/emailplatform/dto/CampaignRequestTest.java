package com.emailplatform.dto;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class CampaignRequestTest {

    @Test
    void testSettersAndGetters() {
        CampaignRequest req = new CampaignRequest();
        req.setName("Test");
        req.setDescription("Desc");
        req.setTemplateId("t1");
        req.setContactList("newsletter");
        LocalDateTime dt = LocalDateTime.now();
        req.setScheduledAt(dt);
        assertEquals("Test", req.getName());
        assertEquals("Desc", req.getDescription());
        assertEquals("t1", req.getTemplateId());
        assertEquals("newsletter", req.getContactList());
        assertEquals(dt, req.getScheduledAt());
    }

    @Test
    void testDefaultConstructor() {
        CampaignRequest req = new CampaignRequest();
        assertNull(req.getName());
        assertNull(req.getDescription());
        assertNull(req.getTemplateId());
        assertNull(req.getContactList());
        assertNull(req.getScheduledAt());
    }

    @Test
    void testNullOptionalFields() {
        CampaignRequest req = new CampaignRequest();
        req.setName("Campaign");
        assertEquals("Campaign", req.getName());
        assertNull(req.getDescription());
        assertNull(req.getTemplateId());
        assertNull(req.getContactList());
        assertNull(req.getScheduledAt());
    }

    @Test
    void testNameCanHaveSpecialChars() {
        CampaignRequest req = new CampaignRequest();
        req.setName("Q4 2024 - Holiday <Sale> & Promotions");
        assertEquals("Q4 2024 - Holiday <Sale> & Promotions", req.getName());
    }

    @Test
    void testDescriptionCanBeLong() {
        CampaignRequest req = new CampaignRequest();
        String longDesc = "A".repeat(5000);
        req.setDescription(longDesc);
        assertEquals(longDesc, req.getDescription());
    }

    @Test
    void testEqualsAndHashCode() {
        CampaignRequest r1 = new CampaignRequest();
        r1.setName("Test");
        CampaignRequest r2 = new CampaignRequest();
        r2.setName("Test");
        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    void testNotEqualsDifferentName() {
        CampaignRequest r1 = new CampaignRequest();
        r1.setName("Test1");
        CampaignRequest r2 = new CampaignRequest();
        r2.setName("Test2");
        assertNotEquals(r1, r2);
    }

    @Test
    void testToString() {
        CampaignRequest req = new CampaignRequest();
        req.setName("Test");
        assertNotNull(req.toString());
    }

    @Test
    void testScheduledAtCanBeFuture() {
        CampaignRequest req = new CampaignRequest();
        LocalDateTime future = LocalDateTime.now().plusDays(7);
        req.setScheduledAt(future);
        assertTrue(req.getScheduledAt().isAfter(LocalDateTime.now()));
    }
}
