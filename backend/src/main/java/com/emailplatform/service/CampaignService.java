package com.emailplatform.service;

import com.emailplatform.dto.CampaignRequest;
import com.emailplatform.model.Campaign;
import com.emailplatform.model.Contact;
import com.emailplatform.model.Template;
import com.emailplatform.model.User;
import com.emailplatform.repository.CampaignRepository;
import com.emailplatform.repository.ContactRepository;
import com.emailplatform.repository.TemplateRepository;
import com.emailplatform.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CampaignService {

    private final CampaignRepository campaignRepository;
    private final TemplateRepository templateRepository;
    private final ContactRepository contactRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final AnalyticsService analyticsService;
    private final String baseUrl;

    public CampaignService(CampaignRepository campaignRepository,
                           TemplateRepository templateRepository,
                           ContactRepository contactRepository,
                           UserRepository userRepository,
                           EmailService emailService,
                           AnalyticsService analyticsService,
                           @Value("${app.base-url}") String baseUrl) {
        this.campaignRepository = campaignRepository;
        this.templateRepository = templateRepository;
        this.contactRepository = contactRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.analyticsService = analyticsService;
        this.baseUrl = baseUrl;
    }

    public List<Campaign> getCampaigns(String userId) {
        List<Campaign> campaigns = campaignRepository.findByUserId(userId);
        for (Campaign c : campaigns) {
            if (c.getSentCount() != null && c.getSentCount() > 0) {
                long opens = analyticsService.getEventCount(c.getId(), "open");
                long clicks = analyticsService.getEventCount(c.getId(), "click");
                c.setOpenedCount((int) opens);
                c.setClickedCount((int) clicks);
            }
        }
        return campaigns;
    }

    public Campaign createCampaign(String userId, CampaignRequest request) {
        Campaign campaign = new Campaign();
        campaign.setName(request.getName());
        campaign.setDescription(request.getDescription());
        campaign.setTemplateId(request.getTemplateId());
        campaign.setContactList(request.getContactList());
        campaign.setUserId(userId);
        return campaignRepository.save(campaign);
    }

    public Campaign updateCampaign(String id, String userId, CampaignRequest request) {
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Campaign not found"));
        if (!campaign.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        campaign.setName(request.getName());
        campaign.setDescription(request.getDescription());
        campaign.setTemplateId(request.getTemplateId());
        campaign.setContactList(request.getContactList());
        if (request.getScheduledAt() != null) {
            campaign.setScheduledAt(request.getScheduledAt());
        }
        return campaignRepository.save(campaign);
    }

    public void deleteCampaign(String id, String userId) {
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Campaign not found"));
        if (!campaign.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        campaignRepository.delete(campaign);
    }

    @Async
    public void sendCampaign(String campaignId, String userId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new RuntimeException("Campaign not found"));
        if (!campaign.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        if ("sending".equals(campaign.getStatus()) || "sent".equals(campaign.getStatus())) {
            throw new RuntimeException("Campaign has already been sent or is currently sending");
        }

        Template template = templateRepository.findById(campaign.getTemplateId())
                .orElseThrow(() -> new RuntimeException("Template not found"));

        List<Contact> contacts;
        String contactList = campaign.getContactList();
        if (contactList != null && !contactList.isEmpty()) {
            contacts = contactRepository.findByUserIdAndList(campaign.getUserId(), contactList);
        } else {
            contacts = contactRepository.findByUserId(campaign.getUserId());
        }

        if (contacts.isEmpty()) {
            throw new RuntimeException("No contacts found for the selected list");
        }

        campaign.setStatus("sending");
        campaignRepository.save(campaign);

        User owner = userRepository.findById(campaign.getUserId()).orElse(null);
        boolean demoMode = owner == null || !"ADMIN".equals(owner.getRole());

        int sentCount = 0;
        int failCount = 0;
        for (Contact contact : contacts) {
            try {
                Map<String, String> variables = new HashMap<>();
                variables.put("email", contact.getEmail());
                variables.put("firstName", contact.getFirstName() != null ? contact.getFirstName() : "");
                variables.put("lastName", contact.getLastName() != null ? contact.getLastName() : "");

                String trackingPixel = "<img src=\"" + baseUrl + "/api/track/open/" + campaignId + "/" + contact.getId() + "\" width=\"1\" height=\"1\" style=\"display:none\" />";
                String bodyWithTracking = template.getBody() + trackingPixel;

                String subject = emailService.processTemplate(template.getSubject(), variables);
                String htmlBody = emailService.processTemplate(bodyWithTracking, variables);

                String clickTrackingBody = htmlBody.replaceAll("href=\"(https?://[^\"]+)\"",
                        "href=\"" + baseUrl + "/api/track/click/" + campaignId + "/" + contact.getId() + "?url=$1\"");

                if (demoMode) {
                    emailService.sendEmailSimulated(contact.getEmail(), subject, clickTrackingBody);
                } else {
                    emailService.sendEmail(contact.getEmail(), subject, clickTrackingBody);
                }
                sentCount++;
            } catch (Exception e) {
                failCount++;
                System.err.println("FAILED to send email to " + contact.getEmail() + ": " + e.getMessage());
            }
        }

        campaign.setSentCount(sentCount);
        campaign.setStatus("sent");
        campaign.setSentAt(LocalDateTime.now());
        if (failCount > 0 && sentCount == 0) {
            campaign.setStatus("failed");
        }
        campaignRepository.save(campaign);
        System.out.println("Campaign " + campaignId + " completed: " + sentCount + " sent, " + failCount + " failed out of " + contacts.size() + " total");
    }

    public void scheduleCampaign(String campaignId, String userId, LocalDateTime scheduledAt) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new RuntimeException("Campaign not found"));
        if (!campaign.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        if ("sent".equals(campaign.getStatus()) || "sending".equals(campaign.getStatus())) {
            throw new RuntimeException("Cannot schedule a campaign that has already been sent");
        }
        campaign.setScheduledAt(scheduledAt);
        campaign.setStatus("scheduled");
        campaignRepository.save(campaign);
    }
}
