package com.emailplatform.repository;

import com.emailplatform.model.AnalyticsEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AnalyticsRepository extends JpaRepository<AnalyticsEvent, String> {
    List<AnalyticsEvent> findByCampaignId(String campaignId);
    List<AnalyticsEvent> findByCampaignIdAndEventType(String campaignId, String eventType);
    List<AnalyticsEvent> findByCampaignIdAndContactId(String campaignId, String contactId);
    long countByCampaignIdAndEventType(String campaignId, String eventType);
}
