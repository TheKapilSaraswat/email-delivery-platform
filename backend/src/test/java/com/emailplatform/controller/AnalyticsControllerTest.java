package com.emailplatform.controller;

import com.emailplatform.service.AnalyticsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AnalyticsController.class)
@AutoConfigureMockMvc(addFilters = false)
class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AnalyticsService analyticsService;

    @Test
    void testGetCampaignStats() throws Exception {
        Map<String, Object> stats = new HashMap<>();
        stats.put("campaign", Map.of("id", "c1", "name", "Campaign One"));
        stats.put("stats", Map.of("sent", 100, "opened", 50, "clicked", 10));
        stats.put("rates", Map.of("open_rate", 50.0, "click_rate", 10.0, "click_to_open_rate", 20.0));
        stats.put("events", java.util.Collections.emptyList());

        when(analyticsService.getCampaignStats(eq("c1"), any())).thenReturn(stats);

        mockMvc.perform(get("/api/analytics/campaigns/c1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.campaign.id").value("c1"))
                .andExpect(jsonPath("$.stats.sent").value(100))
                .andExpect(jsonPath("$.rates.open_rate").value(50.0));
    }

    @Test
    void testGetOverview() throws Exception {
        Map<String, Object> overview = new HashMap<>();
        overview.put("totalCampaigns", 1L);
        overview.put("totalSent", 100L);
        overview.put("totalOpens", 50L);
        overview.put("totalClicks", 10L);
        overview.put("openRate", 50.0);
        overview.put("clickRate", 10.0);

        when(analyticsService.getOverview(any())).thenReturn(overview);

        mockMvc.perform(get("/api/analytics/overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCampaigns").value(1))
                .andExpect(jsonPath("$.openRate").value(50.0));
    }
}
