package com.emailplatform.controller;

import com.emailplatform.dto.CampaignRequest;
import com.emailplatform.model.Campaign;
import com.emailplatform.service.CampaignService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CampaignController.class)
@AutoConfigureMockMvc(addFilters = false)
class CampaignControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CampaignService campaignService;

    @Autowired
    private ObjectMapper objectMapper;

    private Campaign createTestCampaign() {
        Campaign c = new Campaign();
        c.setId("camp-id-1");
        c.setName("Test Campaign");
        c.setDescription("Description");
        c.setStatus("draft");
        c.setSentCount(0);
        c.setOpenedCount(0);
        c.setClickedCount(0);
        c.setUserId("user-id-1");
        return c;
    }

    @Test
    void testGetCampaigns() throws Exception {
        Campaign c = createTestCampaign();
        when(campaignService.getCampaigns(any())).thenReturn(Arrays.asList(c));

        mockMvc.perform(get("/api/campaigns"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Campaign"));
    }

    @Test
    void testGetCampaignsEmpty() throws Exception {
        when(campaignService.getCampaigns(any())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/campaigns"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void testCreateCampaign() throws Exception {
        CampaignRequest req = new CampaignRequest();
        req.setName("New Campaign");
        req.setDescription("Desc");

        Campaign c = createTestCampaign();
        c.setName("New Campaign");
        when(campaignService.createCampaign(any(), any(CampaignRequest.class))).thenReturn(c);

        mockMvc.perform(post("/api/campaigns")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Campaign"));
    }

    @Test
    void testCreateCampaignFailure() throws Exception {
        CampaignRequest req = new CampaignRequest();
        req.setName("Test");

        when(campaignService.createCampaign(any(), any(CampaignRequest.class)))
            .thenThrow(new RuntimeException("Error"));

        mockMvc.perform(post("/api/campaigns")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateCampaign() throws Exception {
        CampaignRequest req = new CampaignRequest();
        req.setName("Updated");

        Campaign c = createTestCampaign();
        c.setName("Updated");
        when(campaignService.updateCampaign(eq("camp-id-1"), any(), any(CampaignRequest.class))).thenReturn(c);

        mockMvc.perform(put("/api/campaigns/camp-id-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));
    }

    @Test
    void testUpdateCampaignNotFound() throws Exception {
        CampaignRequest req = new CampaignRequest();
        req.setName("Test");

        when(campaignService.updateCampaign(eq("nonexistent"), any(), any(CampaignRequest.class)))
            .thenThrow(new RuntimeException("Campaign not found"));

        mockMvc.perform(put("/api/campaigns/nonexistent")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Campaign not found"));
    }

    @Test
    void testDeleteCampaign() throws Exception {
        doNothing().when(campaignService).deleteCampaign(any(), any());

        mockMvc.perform(delete("/api/campaigns/camp-id-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Campaign deleted"));
    }

    @Test
    void testDeleteCampaignNotFound() throws Exception {
        doNothing().when(campaignService).deleteCampaign(any(), any());

        mockMvc.perform(delete("/api/campaigns/nonexistent"))
                .andExpect(status().isOk());
    }

    @Test
    void testSendCampaign() throws Exception {
        doNothing().when(campaignService).sendCampaign(eq("camp-id-1"), any());

        mockMvc.perform(post("/api/campaigns/camp-id-1/send"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Campaign sending started"));
    }

    @Test
    void testSendCampaignFailure() throws Exception {
        org.mockito.Mockito.doThrow(new RuntimeException("Template not found"))
            .when(campaignService).sendCampaign(eq("camp-id-1"), any());

        mockMvc.perform(post("/api/campaigns/camp-id-1/send"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testScheduleCampaign() throws Exception {
        doNothing().when(campaignService).scheduleCampaign(eq("camp-id-1"), any(), any(LocalDateTime.class));

        Map<String, String> body = Map.of("scheduledAt", "2025-12-25T10:00:00");
        mockMvc.perform(post("/api/campaigns/camp-id-1/schedule")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Campaign scheduled"));
    }

    @Test
    void testScheduleCampaignInvalidDate() throws Exception {
        Map<String, String> body = Map.of("scheduledAt", "invalid-date");
        mockMvc.perform(post("/api/campaigns/camp-id-1/schedule")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }
}
