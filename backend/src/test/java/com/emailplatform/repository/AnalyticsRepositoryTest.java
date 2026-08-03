package com.emailplatform.repository;

import com.emailplatform.model.AnalyticsEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class AnalyticsRepositoryTest {

    @Autowired
    private AnalyticsRepository analyticsRepository;

    @Test
    void testSaveAndFind() {
        AnalyticsEvent e = new AnalyticsEvent();
        e.setCampaignId("campaign-1");
        e.setContactId("contact-1");
        e.setEventType("open");
        AnalyticsEvent saved = analyticsRepository.save(e);

        assertNotNull(saved.getId());
        assertTrue(analyticsRepository.findById(saved.getId()).isPresent());
    }

    @Test
    void testFindByCampaignId() {
        AnalyticsEvent e1 = new AnalyticsEvent();
        e1.setCampaignId("c1");
        e1.setContactId("ct1");
        e1.setEventType("open");
        analyticsRepository.save(e1);

        AnalyticsEvent e2 = new AnalyticsEvent();
        e2.setCampaignId("c1");
        e2.setContactId("ct2");
        e2.setEventType("click");
        analyticsRepository.save(e2);

        AnalyticsEvent e3 = new AnalyticsEvent();
        e3.setCampaignId("c2");
        e3.setContactId("ct1");
        e3.setEventType("open");
        analyticsRepository.save(e3);

        List<AnalyticsEvent> result = analyticsRepository.findByCampaignId("c1");
        assertEquals(2, result.size());
    }

    @Test
    void testFindByCampaignIdEmpty() {
        List<AnalyticsEvent> result = analyticsRepository.findByCampaignId("nonexistent");
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindByCampaignIdAndEventType() {
        AnalyticsEvent e1 = new AnalyticsEvent();
        e1.setCampaignId("c1");
        e1.setContactId("ct1");
        e1.setEventType("open");
        analyticsRepository.save(e1);

        AnalyticsEvent e2 = new AnalyticsEvent();
        e2.setCampaignId("c1");
        e2.setContactId("ct2");
        e2.setEventType("click");
        analyticsRepository.save(e2);

        AnalyticsEvent e3 = new AnalyticsEvent();
        e3.setCampaignId("c1");
        e3.setContactId("ct3");
        e3.setEventType("open");
        analyticsRepository.save(e3);

        List<AnalyticsEvent> result = analyticsRepository.findByCampaignIdAndEventType("c1", "open");
        assertEquals(2, result.size());
    }

    @Test
    void testFindByCampaignIdAndContactId() {
        AnalyticsEvent e1 = new AnalyticsEvent();
        e1.setCampaignId("c1");
        e1.setContactId("ct1");
        e1.setEventType("open");
        analyticsRepository.save(e1);

        AnalyticsEvent e2 = new AnalyticsEvent();
        e2.setCampaignId("c1");
        e2.setContactId("ct2");
        e2.setEventType("open");
        analyticsRepository.save(e2);

        List<AnalyticsEvent> result = analyticsRepository.findByCampaignIdAndContactId("c1", "ct1");
        assertEquals(1, result.size());
    }

    @Test
    void testCountByCampaignIdAndEventType() {
        AnalyticsEvent e1 = new AnalyticsEvent();
        e1.setCampaignId("c1");
        e1.setContactId("ct1");
        e1.setEventType("open");
        analyticsRepository.save(e1);

        AnalyticsEvent e2 = new AnalyticsEvent();
        e2.setCampaignId("c1");
        e2.setContactId("ct2");
        e2.setEventType("open");
        analyticsRepository.save(e2);

        AnalyticsEvent e3 = new AnalyticsEvent();
        e3.setCampaignId("c1");
        e3.setContactId("ct3");
        e3.setEventType("click");
        analyticsRepository.save(e3);

        assertEquals(2, analyticsRepository.countByCampaignIdAndEventType("c1", "open"));
        assertEquals(1, analyticsRepository.countByCampaignIdAndEventType("c1", "click"));
        assertEquals(0, analyticsRepository.countByCampaignIdAndEventType("c1", "bounce"));
    }

    @Test
    void testDeleteEvent() {
        AnalyticsEvent e = new AnalyticsEvent();
        e.setCampaignId("c1");
        e.setContactId("ct1");
        e.setEventType("open");
        AnalyticsEvent saved = analyticsRepository.save(e);

        analyticsRepository.deleteById(saved.getId());
        assertFalse(analyticsRepository.findById(saved.getId()).isPresent());
    }

    @Test
    void testCount() {
        assertEquals(0, analyticsRepository.count());

        AnalyticsEvent e = new AnalyticsEvent();
        e.setCampaignId("c1");
        e.setContactId("ct1");
        e.setEventType("open");
        analyticsRepository.save(e);

        assertEquals(1, analyticsRepository.count());
    }

    @Test
    void testMultipleEventTypes() {
        String[] types = {"open", "click", "bounce", "sent"};
        for (String type : types) {
            AnalyticsEvent e = new AnalyticsEvent();
            e.setCampaignId("c1");
            e.setContactId("ct1");
            e.setEventType(type);
            analyticsRepository.save(e);
        }

        assertEquals(4, analyticsRepository.count());
        for (String type : types) {
            assertEquals(1, analyticsRepository.countByCampaignIdAndEventType("c1", type));
        }
    }

    @Test
    void testFindByCampaignIdAndEventTypeEmpty() {
        List<AnalyticsEvent> result = analyticsRepository.findByCampaignIdAndEventType("c1", "open");
        assertTrue(result.isEmpty());
    }
}
