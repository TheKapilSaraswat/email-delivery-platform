package com.emailplatform.service;

import com.emailplatform.model.AnalyticsEvent;
import com.emailplatform.model.Campaign;
import com.emailplatform.model.Contact;
import com.emailplatform.repository.AnalyticsRepository;
import com.emailplatform.repository.CampaignRepository;
import com.emailplatform.repository.ContactRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private final AnalyticsRepository analyticsRepository;
    private final CampaignRepository campaignRepository;
    private final ContactRepository contactRepository;

    public AnalyticsService(AnalyticsRepository analyticsRepository, CampaignRepository campaignRepository, ContactRepository contactRepository) {
        this.analyticsRepository = analyticsRepository;
        this.campaignRepository = campaignRepository;
        this.contactRepository = contactRepository;
    }

    public void trackEvent(String campaignId, String contactId, String eventType, String url) {
        AnalyticsEvent event = new AnalyticsEvent();
        event.setCampaignId(campaignId);
        event.setContactId(contactId);
        event.setEventType(eventType);
        event.setUrl(url);
        analyticsRepository.save(event);
    }

    public long getEventCount(String campaignId, String eventType) {
        return analyticsRepository.countByCampaignIdAndEventType(campaignId, eventType);
    }

    public Map<String, Object> getCampaignStats(String campaignId, String userId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new RuntimeException("Campaign not found"));
        if (!campaign.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        long sent = analyticsRepository.countByCampaignIdAndEventType(campaignId, "sent");
        long opens = analyticsRepository.countByCampaignIdAndEventType(campaignId, "open");
        long clicks = analyticsRepository.countByCampaignIdAndEventType(campaignId, "click");

        List<AnalyticsEvent> events = analyticsRepository.findByCampaignId(campaignId);

        List<String> contactIds = events.stream()
                .map(AnalyticsEvent::getContactId)
                .distinct()
                .collect(Collectors.toList());

        Map<String, String> contactEmailMap = new HashMap<>();
        if (!contactIds.isEmpty()) {
            List<Contact> contacts = contactRepository.findByIdIn(contactIds);
            for (Contact c : contacts) {
                contactEmailMap.put(c.getId(), c.getEmail());
            }
        }

        List<Map<String, Object>> eventList = new ArrayList<>();
        for (AnalyticsEvent event : events) {
            Map<String, Object> eventMap = new HashMap<>();
            eventMap.put("id", event.getId());
            eventMap.put("email", contactEmailMap.getOrDefault(event.getContactId(), "unknown"));
            eventMap.put("event", event.getEventType());
            eventMap.put("timestamp", event.getTimestamp());
            eventList.add(eventMap);
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("sent", campaign.getSentCount() != null ? campaign.getSentCount() : sent);
        stats.put("opened", opens);
        stats.put("clicked", clicks);

        double openRate = sent > 0 ? (double) opens / sent * 100 : 0;
        double clickRate = sent > 0 ? (double) clicks / sent * 100 : 0;
        double clickToOpenRate = opens > 0 ? (double) clicks / opens * 100 : 0;

        Map<String, Object> rates = new HashMap<>();
        rates.put("open_rate", Math.round(openRate * 10.0) / 10.0);
        rates.put("click_rate", Math.round(clickRate * 10.0) / 10.0);
        rates.put("click_to_open_rate", Math.round(clickToOpenRate * 10.0) / 10.0);

        Map<String, Object> campaignInfo = new HashMap<>();
        campaignInfo.put("id", campaign.getId());
        campaignInfo.put("name", campaign.getName());
        campaignInfo.put("description", campaign.getDescription());
        campaignInfo.put("status", campaign.getStatus());
        campaignInfo.put("contactList", campaign.getContactList());
        campaignInfo.put("sentAt", campaign.getSentAt());

        Map<String, Object> result = new HashMap<>();
        result.put("campaign", campaignInfo);
        result.put("stats", stats);
        result.put("rates", rates);
        result.put("events", eventList);
        return result;
    }

    public Map<String, Object> getOverview(String userId) {
        List<Campaign> userCampaigns = campaignRepository.findByUserId(userId);
        long totalCampaigns = userCampaigns.size();
        long totalSent = 0;
        long totalOpens = 0;
        long totalClicks = 0;

        for (Campaign c : userCampaigns) {
            if (c.getSentCount() != null) {
                totalSent += c.getSentCount();
            }
            totalOpens += analyticsRepository.countByCampaignIdAndEventType(c.getId(), "open");
            totalClicks += analyticsRepository.countByCampaignIdAndEventType(c.getId(), "click");
        }

        Map<String, Object> overview = new HashMap<>();
        overview.put("totalCampaigns", totalCampaigns);
        overview.put("totalSent", totalSent);
        overview.put("totalOpens", totalOpens);
        overview.put("totalClicks", totalClicks);
        overview.put("openRate", totalSent > 0 ? Math.round((double) totalOpens / totalSent * 1000.0) / 10.0 : 0);
        overview.put("clickRate", totalSent > 0 ? Math.round((double) totalClicks / totalSent * 1000.0) / 10.0 : 0);
        return overview;
    }
}
