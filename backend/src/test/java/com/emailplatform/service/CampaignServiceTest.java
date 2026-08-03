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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CampaignServiceTest {

    @Mock
    private CampaignRepository campaignRepository;
    @Mock
    private TemplateRepository templateRepository;
    @Mock
    private ContactRepository contactRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private EmailService emailService;
    @Mock
    private AnalyticsService analyticsService;

    @InjectMocks
    private CampaignService campaignService;

    private Campaign testCampaign;
    private Template testTemplate;
    private Contact testContact;
    private User testUser;

    @BeforeEach
    void setUp() {
        testCampaign = new Campaign();
        testCampaign.setId("camp-id-1");
        testCampaign.setName("Test Campaign");
        testCampaign.setDescription("Desc");
        testCampaign.setTemplateId("tmpl-id-1");
        testCampaign.setUserId("user-id-1");
        testCampaign.setContactList("newsletter");
        testCampaign.setStatus("draft");
        testCampaign.setSentCount(0);
        testCampaign.setOpenedCount(0);
        testCampaign.setClickedCount(0);

        testTemplate = new Template();
        testTemplate.setId("tmpl-id-1");
        testTemplate.setName("Welcome");
        testTemplate.setSubject("Hello {{firstName}}");
        testTemplate.setBody("<p>Welcome {{firstName}}!</p>");

        testContact = new Contact();
        testContact.setId("contact-id-1");
        testContact.setEmail("contact@test.com");
        testContact.setFirstName("John");
        testContact.setLastName("Doe");
        testContact.setUserId("user-id-1");
        testContact.setList("newsletter");

        testUser = new User();
        testUser.setId("user-id-1");
        testUser.setEmail("user@test.com");
        testUser.setName("Test User");
        testUser.setRole("USER");
    }

    @Test
    void testGetCampaigns() {
        testCampaign.setSentCount(10);
        List<Campaign> campaigns = Arrays.asList(testCampaign);
        when(campaignRepository.findByUserId("user-id-1")).thenReturn(campaigns);
        when(analyticsService.getEventCount("camp-id-1", "open")).thenReturn(5L);
        when(analyticsService.getEventCount("camp-id-1", "click")).thenReturn(2L);

        List<Campaign> result = campaignService.getCampaigns("user-id-1");

        assertEquals(1, result.size());
        assertEquals(5, result.get(0).getOpenedCount());
        assertEquals(2, result.get(0).getClickedCount());
    }

    @Test
    void testGetCampaignsEmpty() {
        when(campaignRepository.findByUserId("user-id-1")).thenReturn(Arrays.asList());

        List<Campaign> result = campaignService.getCampaigns("user-id-1");

        assertTrue(result.isEmpty());
    }

    @Test
    void testGetCampaignsSkipsAnalyticsWhenZeroSent() {
        testCampaign.setSentCount(0);
        when(campaignRepository.findByUserId("user-id-1")).thenReturn(Arrays.asList(testCampaign));

        List<Campaign> result = campaignService.getCampaigns("user-id-1");

        assertEquals(1, result.size());
        verify(analyticsService, never()).getEventCount(anyString(), anyString());
    }

    @Test
    void testCreateCampaign() {
        CampaignRequest req = new CampaignRequest();
        req.setName("New Campaign");
        req.setDescription("New Desc");
        req.setTemplateId("tmpl-1");
        req.setContactList("newsletter");

        when(campaignRepository.save(any(Campaign.class))).thenAnswer(inv -> {
            Campaign c = inv.getArgument(0);
            c.setId("new-id");
            return c;
        });

        Campaign result = campaignService.createCampaign("user-id-1", req);

        assertNotNull(result);
        assertEquals("New Campaign", result.getName());
        assertEquals("New Desc", result.getDescription());
        assertEquals("newsletter", result.getContactList());
        assertEquals("user-id-1", result.getUserId());
        verify(campaignRepository).save(any(Campaign.class));
    }

    @Test
    void testUpdateCampaignSuccess() {
        when(campaignRepository.findById("camp-id-1")).thenReturn(Optional.of(testCampaign));
        when(campaignRepository.save(any(Campaign.class))).thenReturn(testCampaign);

        CampaignRequest req = new CampaignRequest();
        req.setName("Updated Campaign");
        req.setDescription("Updated Desc");
        req.setTemplateId("tmpl-2");
        req.setContactList("vip");

        Campaign result = campaignService.updateCampaign("camp-id-1", "user-id-1", req);

        assertEquals("Updated Campaign", result.getName());
        assertEquals("Updated Desc", result.getDescription());
    }

    @Test
    void testUpdateCampaignNotFound() {
        when(campaignRepository.findById("nonexistent")).thenReturn(Optional.empty());

        CampaignRequest req = new CampaignRequest();
        req.setName("Test");

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> campaignService.updateCampaign("nonexistent", "user-id-1", req));
        assertEquals("Campaign not found", ex.getMessage());
    }

    @Test
    void testUpdateCampaignUnauthorized() {
        when(campaignRepository.findById("camp-id-1")).thenReturn(Optional.of(testCampaign));

        CampaignRequest req = new CampaignRequest();
        req.setName("Test");

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> campaignService.updateCampaign("camp-id-1", "wrong-user", req));
        assertEquals("Unauthorized", ex.getMessage());
    }

    @Test
    void testUpdateCampaignWithScheduledAt() {
        when(campaignRepository.findById("camp-id-1")).thenReturn(Optional.of(testCampaign));
        when(campaignRepository.save(any(Campaign.class))).thenReturn(testCampaign);

        LocalDateTime sched = LocalDateTime.now().plusDays(1);
        CampaignRequest req = new CampaignRequest();
        req.setName("Test");
        req.setScheduledAt(sched);

        Campaign result = campaignService.updateCampaign("camp-id-1", "user-id-1", req);

        assertEquals(sched, result.getScheduledAt());
    }

    @Test
    void testDeleteCampaignSuccess() {
        when(campaignRepository.findById("camp-id-1")).thenReturn(Optional.of(testCampaign));

        campaignService.deleteCampaign("camp-id-1", "user-id-1");

        verify(campaignRepository).delete(testCampaign);
    }

    @Test
    void testDeleteCampaignNotFound() {
        when(campaignRepository.findById("nonexistent")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> campaignService.deleteCampaign("nonexistent", "user-id-1"));
        assertEquals("Campaign not found", ex.getMessage());
    }

    @Test
    void testDeleteCampaignUnauthorized() {
        when(campaignRepository.findById("camp-id-1")).thenReturn(Optional.of(testCampaign));

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> campaignService.deleteCampaign("camp-id-1", "wrong-user"));
        assertEquals("Unauthorized", ex.getMessage());
    }

    @Test
    void testScheduleCampaign() {
        when(campaignRepository.findById("camp-id-1")).thenReturn(Optional.of(testCampaign));
        when(campaignRepository.save(any(Campaign.class))).thenReturn(testCampaign);

        LocalDateTime sched = LocalDateTime.now().plusDays(7);
        campaignService.scheduleCampaign("camp-id-1", "user-id-1", sched);

        assertEquals(sched, testCampaign.getScheduledAt());
        assertEquals("scheduled", testCampaign.getStatus());
        verify(campaignRepository).save(testCampaign);
    }

    @Test
    void testScheduleCampaignNotFound() {
        when(campaignRepository.findById("nonexistent")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> campaignService.scheduleCampaign("nonexistent", "user-id-1", LocalDateTime.now()));
        assertEquals("Campaign not found", ex.getMessage());
    }

    @Test
    void testSendCampaignWithTemplateAndContacts() {
        testCampaign.setContactList("newsletter");
        when(campaignRepository.findById("camp-id-1")).thenReturn(Optional.of(testCampaign));
        when(templateRepository.findById("tmpl-id-1")).thenReturn(Optional.of(testTemplate));
        when(contactRepository.findByUserIdAndList("user-id-1", "newsletter")).thenReturn(Arrays.asList(testContact));
        when(userRepository.findById("user-id-1")).thenReturn(Optional.of(testUser));
        when(emailService.processTemplate(anyString(), anyMap())).thenAnswer(inv -> inv.getArgument(0));
        when(campaignRepository.save(any(Campaign.class))).thenReturn(testCampaign);

        campaignService.sendCampaign("camp-id-1", "user-id-1");

        verify(emailService).sendEmailSimulated(eq("contact@test.com"), anyString(), anyString());
        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
        verify(campaignRepository, atLeastOnce()).save(any(Campaign.class));
    }

    @Test
    void testSendCampaignRealForAdmin() {
        testUser.setRole("ADMIN");
        testCampaign.setContactList("newsletter");
        when(campaignRepository.findById("camp-id-1")).thenReturn(Optional.of(testCampaign));
        when(templateRepository.findById("tmpl-id-1")).thenReturn(Optional.of(testTemplate));
        when(contactRepository.findByUserIdAndList("user-id-1", "newsletter")).thenReturn(Arrays.asList(testContact));
        when(userRepository.findById("user-id-1")).thenReturn(Optional.of(testUser));
        when(emailService.processTemplate(anyString(), anyMap())).thenAnswer(inv -> inv.getArgument(0));
        when(campaignRepository.save(any(Campaign.class))).thenReturn(testCampaign);

        campaignService.sendCampaign("camp-id-1", "user-id-1");

        verify(emailService).sendEmail(eq("contact@test.com"), anyString(), anyString());
        verify(emailService, never()).sendEmailSimulated(anyString(), anyString(), anyString());
    }

    @Test
    void testSendCampaignNoContactList() {
        testCampaign.setContactList(null);
        when(campaignRepository.findById("camp-id-1")).thenReturn(Optional.of(testCampaign));
        when(templateRepository.findById("tmpl-id-1")).thenReturn(Optional.of(testTemplate));
        when(contactRepository.findByUserId("user-id-1")).thenReturn(Arrays.asList(testContact));
        when(userRepository.findById("user-id-1")).thenReturn(Optional.of(testUser));
        when(emailService.processTemplate(anyString(), anyMap())).thenAnswer(inv -> inv.getArgument(0));
        when(campaignRepository.save(any(Campaign.class))).thenReturn(testCampaign);

        campaignService.sendCampaign("camp-id-1", "user-id-1");

        verify(contactRepository).findByUserId("user-id-1");
        verify(emailService).sendEmailSimulated(eq("contact@test.com"), anyString(), anyString());
    }

    @Test
    void testSendCampaignEmptyContactList() {
        testCampaign.setContactList("");
        when(campaignRepository.findById("camp-id-1")).thenReturn(Optional.of(testCampaign));
        when(templateRepository.findById("tmpl-id-1")).thenReturn(Optional.of(testTemplate));
        when(contactRepository.findByUserId("user-id-1")).thenReturn(Arrays.asList(testContact));
        when(userRepository.findById("user-id-1")).thenReturn(Optional.of(testUser));
        when(emailService.processTemplate(anyString(), anyMap())).thenAnswer(inv -> inv.getArgument(0));
        when(campaignRepository.save(any(Campaign.class))).thenReturn(testCampaign);

        campaignService.sendCampaign("camp-id-1", "user-id-1");

        verify(contactRepository).findByUserId("user-id-1");
    }

    @Test
    void testSendCampaignNotFound() {
        when(campaignRepository.findById("nonexistent")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> campaignService.sendCampaign("nonexistent", "user-id-1"));
        assertEquals("Campaign not found", ex.getMessage());
    }

    @Test
    void testSendCampaignTemplateNotFound() {
        when(campaignRepository.findById("camp-id-1")).thenReturn(Optional.of(testCampaign));
        when(templateRepository.findById("tmpl-id-1")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> campaignService.sendCampaign("camp-id-1", "user-id-1"));
        assertEquals("Template not found", ex.getMessage());
    }

    @Test
    void testSendCampaignEmailFailureContinues() {
        testUser.setRole("ADMIN");
        when(campaignRepository.findById("camp-id-1")).thenReturn(Optional.of(testCampaign));
        when(templateRepository.findById("tmpl-id-1")).thenReturn(Optional.of(testTemplate));
        when(contactRepository.findByUserIdAndList("user-id-1", "newsletter")).thenReturn(Arrays.asList(testContact));
        when(userRepository.findById("user-id-1")).thenReturn(Optional.of(testUser));
        when(emailService.processTemplate(anyString(), anyMap())).thenAnswer(inv -> inv.getArgument(0));
        doThrow(new RuntimeException("SMTP error")).when(emailService).sendEmail(anyString(), anyString(), anyString());
        when(campaignRepository.save(any(Campaign.class))).thenReturn(testCampaign);

        campaignService.sendCampaign("camp-id-1", "user-id-1");

        verify(campaignRepository, atLeastOnce()).save(any(Campaign.class));
    }

    @Test
    void testSendCampaignMultipleContacts() {
        Contact c1 = new Contact();
        c1.setId("c1");
        c1.setEmail("c1@test.com");
        c1.setFirstName("One");
        c1.setUserId("user-id-1");
        c1.setList("newsletter");

        Contact c2 = new Contact();
        c2.setId("c2");
        c2.setEmail("c2@test.com");
        c2.setFirstName("Two");
        c2.setUserId("user-id-1");
        c2.setList("newsletter");

        when(campaignRepository.findById("camp-id-1")).thenReturn(Optional.of(testCampaign));
        when(templateRepository.findById("tmpl-id-1")).thenReturn(Optional.of(testTemplate));
        when(contactRepository.findByUserIdAndList("user-id-1", "newsletter")).thenReturn(Arrays.asList(c1, c2));
        when(userRepository.findById("user-id-1")).thenReturn(Optional.of(testUser));
        when(emailService.processTemplate(anyString(), anyMap())).thenAnswer(inv -> inv.getArgument(0));
        when(campaignRepository.save(any(Campaign.class))).thenReturn(testCampaign);

        campaignService.sendCampaign("camp-id-1", "user-id-1");

        verify(emailService, times(2)).sendEmailSimulated(anyString(), anyString(), anyString());
    }

    @Test
    void testSendCampaignSetsSentStatus() {
        when(campaignRepository.findById("camp-id-1")).thenReturn(Optional.of(testCampaign));
        when(templateRepository.findById("tmpl-id-1")).thenReturn(Optional.of(testTemplate));
        when(contactRepository.findByUserIdAndList("user-id-1", "newsletter")).thenReturn(Arrays.asList(testContact));
        when(userRepository.findById("user-id-1")).thenReturn(Optional.of(testUser));
        when(emailService.processTemplate(anyString(), anyMap())).thenAnswer(inv -> inv.getArgument(0));
        when(campaignRepository.save(any(Campaign.class))).thenReturn(testCampaign);

        campaignService.sendCampaign("camp-id-1", "user-id-1");

        verify(campaignRepository, atLeastOnce()).save(argThat(c -> {
            Campaign camp = (Campaign) c;
            return "sent".equals(camp.getStatus()) || "sending".equals(camp.getStatus());
        }));
    }
}
