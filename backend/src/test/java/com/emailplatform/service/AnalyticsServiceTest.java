package com.emailplatform.service;

import com.emailplatform.model.AnalyticsEvent;
import com.emailplatform.model.Campaign;
import com.emailplatform.model.Contact;
import com.emailplatform.repository.AnalyticsRepository;
import com.emailplatform.repository.CampaignRepository;
import com.emailplatform.repository.ContactRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private AnalyticsRepository analyticsRepository;
    @Mock
    private CampaignRepository campaignRepository;
    @Mock
    private ContactRepository contactRepository;

    @InjectMocks
    private AnalyticsService analyticsService;

    private Campaign testCampaign;

    @BeforeEach
    void setUp() {
        testCampaign = new Campaign();
        testCampaign.setId("c1");
        testCampaign.setUserId("user-id-1");
        testCampaign.setName("Campaign One");
        testCampaign.setDescription("Desc");
        testCampaign.setStatus("sent");
        testCampaign.setSentCount(100);
    }

    @Test
    void testTrackEvent() {
        when(analyticsRepository.save(any(AnalyticsEvent.class))).thenAnswer(inv -> {
            AnalyticsEvent e = inv.getArgument(0);
            e.setId("event-id");
            return e;
        });

        analyticsService.trackEvent("campaign-1", "contact-1", "open", null);

        verify(analyticsRepository).save(any(AnalyticsEvent.class));
    }

    @Test
    void testTrackEventWithUrl() {
        when(analyticsRepository.save(any(AnalyticsEvent.class))).thenAnswer(inv -> {
            AnalyticsEvent e = inv.getArgument(0);
            e.setId("event-id");
            return e;
        });

        analyticsService.trackEvent("campaign-1", "contact-1", "click", "http://example.com");

        verify(analyticsRepository).save(argThat(e ->
            "click".equals(e.getEventType()) && "http://example.com".equals(e.getUrl())
        ));
    }

    @Test
    void testGetEventCount() {
        when(analyticsRepository.countByCampaignIdAndEventType("campaign-1", "open")).thenReturn(10L);

        long count = analyticsService.getEventCount("campaign-1", "open");

        assertEquals(10L, count);
    }

    @Test
    void testGetEventCountZero() {
        when(analyticsRepository.countByCampaignIdAndEventType("campaign-1", "bounce")).thenReturn(0L);

        long count = analyticsService.getEventCount("campaign-1", "bounce");

        assertEquals(0L, count);
    }

    @Test
    void testGetCampaignStats() {
        when(campaignRepository.findById("c1")).thenReturn(Optional.of(testCampaign));
        when(analyticsRepository.countByCampaignIdAndEventType("c1", "sent")).thenReturn(100L);
        when(analyticsRepository.countByCampaignIdAndEventType("c1", "open")).thenReturn(50L);
        when(analyticsRepository.countByCampaignIdAndEventType("c1", "click")).thenReturn(10L);
        when(analyticsRepository.findByCampaignId("c1")).thenReturn(Collections.emptyList());

        Map<String, Object> stats = analyticsService.getCampaignStats("c1", "user-id-1");

        @SuppressWarnings("unchecked")
        Map<String, Object> campaign = (Map<String, Object>) stats.get("campaign");
        @SuppressWarnings("unchecked")
        Map<String, Object> counts = (Map<String, Object>) stats.get("stats");
        @SuppressWarnings("unchecked")
        Map<String, Object> rates = (Map<String, Object>) stats.get("rates");

        assertEquals("c1", campaign.get("id"));
        assertEquals("Campaign One", campaign.get("name"));
        assertEquals(100L, counts.get("sent"));
        assertEquals(50L, counts.get("opened"));
        assertEquals(10L, counts.get("clicked"));
        assertEquals(50.0, rates.get("open_rate"));
        assertEquals(10.0, rates.get("click_rate"));
        assertEquals(20.0, rates.get("click_to_open_rate"));
        assertEquals(0, ((List<?>) stats.get("events")).size());
    }

    @Test
    void testGetCampaignStatsZeroSent() {
        testCampaign.setSentCount(null);
        when(campaignRepository.findById("c1")).thenReturn(Optional.of(testCampaign));
        when(analyticsRepository.countByCampaignIdAndEventType("c1", "sent")).thenReturn(0L);
        when(analyticsRepository.countByCampaignIdAndEventType("c1", "open")).thenReturn(0L);
        when(analyticsRepository.countByCampaignIdAndEventType("c1", "click")).thenReturn(0L);
        when(analyticsRepository.findByCampaignId("c1")).thenReturn(Collections.emptyList());

        Map<String, Object> stats = analyticsService.getCampaignStats("c1", "user-id-1");

        @SuppressWarnings("unchecked")
        Map<String, Object> rates = (Map<String, Object>) stats.get("rates");
        assertEquals(0.0, rates.get("open_rate"));
        assertEquals(0.0, rates.get("click_rate"));
        assertEquals(0.0, rates.get("click_to_open_rate"));
    }

    @Test
    void testGetCampaignStatsZeroOpens() {
        testCampaign.setSentCount(100);
        when(campaignRepository.findById("c1")).thenReturn(Optional.of(testCampaign));
        when(analyticsRepository.countByCampaignIdAndEventType("c1", "sent")).thenReturn(100L);
        when(analyticsRepository.countByCampaignIdAndEventType("c1", "open")).thenReturn(0L);
        when(analyticsRepository.countByCampaignIdAndEventType("c1", "click")).thenReturn(0L);
        when(analyticsRepository.findByCampaignId("c1")).thenReturn(Collections.emptyList());

        Map<String, Object> stats = analyticsService.getCampaignStats("c1", "user-id-1");

        @SuppressWarnings("unchecked")
        Map<String, Object> rates = (Map<String, Object>) stats.get("rates");
        assertEquals(0.0, rates.get("click_to_open_rate"));
        assertEquals(0.0, rates.get("open_rate"));
    }

    @Test
    void testGetCampaignStatsHighRate() {
        testCampaign.setSentCount(10);
        when(campaignRepository.findById("c1")).thenReturn(Optional.of(testCampaign));
        when(analyticsRepository.countByCampaignIdAndEventType("c1", "sent")).thenReturn(10L);
        when(analyticsRepository.countByCampaignIdAndEventType("c1", "open")).thenReturn(9L);
        when(analyticsRepository.countByCampaignIdAndEventType("c1", "click")).thenReturn(8L);
        when(analyticsRepository.findByCampaignId("c1")).thenReturn(Collections.emptyList());

        Map<String, Object> stats = analyticsService.getCampaignStats("c1", "user-id-1");

        @SuppressWarnings("unchecked")
        Map<String, Object> rates = (Map<String, Object>) stats.get("rates");
        assertEquals(90.0, rates.get("open_rate"));
        assertEquals(80.0, rates.get("click_rate"));
        assertEquals(88.9, rates.get("click_to_open_rate"));
    }

    @Test
    void testGetCampaignStatsUnauthorized() {
        when(campaignRepository.findById("c1")).thenReturn(Optional.of(testCampaign));

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> analyticsService.getCampaignStats("c1", "other-user"));
        assertEquals("Unauthorized", ex.getMessage());
    }

    @Test
    void testGetCampaignStatsNotFound() {
        when(campaignRepository.findById("missing")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> analyticsService.getCampaignStats("missing", "user-id-1"));
        assertEquals("Campaign not found", ex.getMessage());
    }

    @Test
    void testGetCampaignStatsWithEventsResolvesEmails() {
        AnalyticsEvent open = new AnalyticsEvent();
        open.setId("e1");
        open.setCampaignId("c1");
        open.setContactId("contact-1");
        open.setEventType("open");

        Contact contact = new Contact();
        contact.setId("contact-1");
        contact.setEmail("person@test.com");

        when(campaignRepository.findById("c1")).thenReturn(Optional.of(testCampaign));
        when(analyticsRepository.countByCampaignIdAndEventType("c1", "sent")).thenReturn(100L);
        when(analyticsRepository.countByCampaignIdAndEventType("c1", "open")).thenReturn(1L);
        when(analyticsRepository.countByCampaignIdAndEventType("c1", "click")).thenReturn(0L);
        when(analyticsRepository.findByCampaignId("c1")).thenReturn(Arrays.asList(open));
        when(contactRepository.findByIdIn(anyList())).thenReturn(Arrays.asList(contact));

        Map<String, Object> stats = analyticsService.getCampaignStats("c1", "user-id-1");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> events = (List<Map<String, Object>>) stats.get("events");
        assertEquals(1, events.size());
        assertEquals("person@test.com", events.get(0).get("email"));
        assertEquals("open", events.get(0).get("event"));
    }

    @Test
    void testGetOverview() {
        testCampaign.setSentCount(100);
        when(campaignRepository.findByUserId("user-id-1")).thenReturn(Arrays.asList(testCampaign));
        when(analyticsRepository.countByCampaignIdAndEventType("c1", "open")).thenReturn(50L);
        when(analyticsRepository.countByCampaignIdAndEventType("c1", "click")).thenReturn(10L);

        Map<String, Object> overview = analyticsService.getOverview("user-id-1");

        assertEquals(1L, overview.get("totalCampaigns"));
        assertEquals(100L, overview.get("totalSent"));
        assertEquals(50L, overview.get("totalOpens"));
        assertEquals(10L, overview.get("totalClicks"));
        assertEquals(50.0, overview.get("openRate"));
        assertEquals(10.0, overview.get("clickRate"));
    }

    @Test
    void testGetOverviewEmpty() {
        when(campaignRepository.findByUserId("user-id-1")).thenReturn(Collections.emptyList());

        Map<String, Object> overview = analyticsService.getOverview("user-id-1");

        assertEquals(0L, overview.get("totalCampaigns"));
        assertEquals(0L, overview.get("totalSent"));
        assertEquals(0L, overview.get("totalOpens"));
        assertEquals(0L, overview.get("totalClicks"));
        assertEquals(0.0, overview.get("openRate"));
        assertEquals(0.0, overview.get("clickRate"));
    }

    @Test
    void testGetOverviewMultipleCampaigns() {
        Campaign c2 = new Campaign();
        c2.setId("c2");
        c2.setUserId("user-id-1");
        c2.setSentCount(50);

        when(campaignRepository.findByUserId("user-id-1")).thenReturn(Arrays.asList(testCampaign, c2));
        when(analyticsRepository.countByCampaignIdAndEventType("c1", "open")).thenReturn(50L);
        when(analyticsRepository.countByCampaignIdAndEventType("c1", "click")).thenReturn(10L);
        when(analyticsRepository.countByCampaignIdAndEventType("c2", "open")).thenReturn(10L);
        when(analyticsRepository.countByCampaignIdAndEventType("c2", "click")).thenReturn(0L);

        Map<String, Object> overview = analyticsService.getOverview("user-id-1");

        assertEquals(2L, overview.get("totalCampaigns"));
        assertEquals(150L, overview.get("totalSent"));
        assertEquals(60L, overview.get("totalOpens"));
        assertEquals(10L, overview.get("totalClicks"));
    }
}
