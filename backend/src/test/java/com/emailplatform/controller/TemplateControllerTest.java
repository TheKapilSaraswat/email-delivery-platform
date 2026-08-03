package com.emailplatform.controller;

import com.emailplatform.dto.TemplateRequest;
import com.emailplatform.model.Template;
import com.emailplatform.service.TemplateService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TemplateController.class)
@AutoConfigureMockMvc(addFilters = false)
class TemplateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TemplateService templateService;

    @Autowired
    private ObjectMapper objectMapper;

    private Template createTestTemplate() {
        Template t = new Template();
        t.setId("tmpl-id-1");
        t.setName("Welcome");
        t.setSubject("Hello {{name}}");
        t.setBody("<p>Welcome!</p>");
        t.setUserId("user-id-1");
        return t;
    }

    @Test
    void testGetTemplates() throws Exception {
        Template t = createTestTemplate();
        when(templateService.getTemplates(any())).thenReturn(Arrays.asList(t));

        mockMvc.perform(get("/api/templates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Welcome"));
    }

    @Test
    void testGetTemplatesEmpty() throws Exception {
        when(templateService.getTemplates(any())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/templates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void testCreateTemplate() throws Exception {
        TemplateRequest req = new TemplateRequest();
        req.setName("Newsletter");
        req.setSubject("Subject");
        req.setBody("<p>Body</p>");

        Template t = createTestTemplate();
        t.setName("Newsletter");
        when(templateService.createTemplate(any(), any(TemplateRequest.class))).thenReturn(t);

        mockMvc.perform(post("/api/templates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Newsletter"));
    }

    @Test
    void testCreateTemplateFailure() throws Exception {
        TemplateRequest req = new TemplateRequest();
        req.setName("Test");
        req.setSubject("Subj");
        req.setBody("Body");

        when(templateService.createTemplate(any(), any(TemplateRequest.class)))
            .thenThrow(new RuntimeException("Error"));

        mockMvc.perform(post("/api/templates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateTemplateInvalidInput() throws Exception {
        mockMvc.perform(post("/api/templates")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateTemplate() throws Exception {
        TemplateRequest req = new TemplateRequest();
        req.setName("Updated");
        req.setSubject("Updated Subject");
        req.setBody("<p>Updated</p>");

        Template t = createTestTemplate();
        t.setName("Updated");
        when(templateService.updateTemplate(eq("tmpl-id-1"), any(), any(TemplateRequest.class))).thenReturn(t);

        mockMvc.perform(put("/api/templates/tmpl-id-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));
    }

    @Test
    void testUpdateTemplateNotFound() throws Exception {
        TemplateRequest req = new TemplateRequest();
        req.setName("Test");
        req.setSubject("Subj");
        req.setBody("Body");

        when(templateService.updateTemplate(eq("nonexistent"), any(), any(TemplateRequest.class)))
            .thenThrow(new RuntimeException("Template not found"));

        mockMvc.perform(put("/api/templates/nonexistent")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Template not found"));
    }

    @Test
    void testDeleteTemplate() throws Exception {
        doNothing().when(templateService).deleteTemplate(any(), any());

        mockMvc.perform(delete("/api/templates/tmpl-id-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Template deleted"));
    }

    @Test
    void testDeleteTemplateNotFound() throws Exception {
        doNothing().when(templateService).deleteTemplate(any(), any());

        mockMvc.perform(delete("/api/templates/nonexistent"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetMultipleTemplates() throws Exception {
        Template t1 = createTestTemplate();
        Template t2 = new Template();
        t2.setId("tmpl-id-2");
        t2.setName("Newsletter");
        when(templateService.getTemplates(any())).thenReturn(Arrays.asList(t1, t2));

        mockMvc.perform(get("/api/templates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Welcome"))
                .andExpect(jsonPath("$[1].name").value("Newsletter"));
    }
}
