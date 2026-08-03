package com.emailplatform.controller;

import com.emailplatform.service.AnalyticsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TrackingController.class)
@AutoConfigureMockMvc(addFilters = false)
class TrackingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AnalyticsService analyticsService;

    @Test
    void testTrackOpen() throws Exception {
        mockMvc.perform(get("/api/track/open/campaign-1/contact-1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/gif"));
    }

    @Test
    void testTrackClick() throws Exception {
        mockMvc.perform(get("/api/track/click/campaign-1/contact-1")
                .param("url", "https://example.com"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://example.com"));
    }

    @Test
    void testTrackOpenDifferentCampaign() throws Exception {
        mockMvc.perform(get("/api/track/open/campaign-2/contact-2"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/gif"));
    }

    @Test
    void testTrackClickDifferentUrl() throws Exception {
        mockMvc.perform(get("/api/track/click/campaign-1/contact-1")
                .param("url", "https://other.com"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://other.com"));
    }

    @Test
    void testTrackClickMissingUrl() throws Exception {
        mockMvc.perform(get("/api/track/click/campaign-1/contact-1"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testTrackOpenReturns1x1Gif() throws Exception {
        byte[] result = mockMvc.perform(get("/api/track/open/c1/ct1"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray();

        // 1x1 transparent GIF is 42 bytes (base64 decoded)
        org.junit.jupiter.api.Assertions.assertEquals(42, result.length);
    }

    @Test
    void testTrackOpenFirstBytesAreGif() throws Exception {
        byte[] result = mockMvc.perform(get("/api/track/open/c1/ct1"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray();

        // GIF89a header
        org.junit.jupiter.api.Assertions.assertEquals((byte) 'G', result[0]);
        org.junit.jupiter.api.Assertions.assertEquals((byte) 'I', result[1]);
        org.junit.jupiter.api.Assertions.assertEquals((byte) 'F', result[2]);
    }
}
