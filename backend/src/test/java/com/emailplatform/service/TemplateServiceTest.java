package com.emailplatform.service;

import com.emailplatform.dto.TemplateRequest;
import com.emailplatform.model.Template;
import com.emailplatform.repository.TemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TemplateServiceTest {

    @Mock
    private TemplateRepository templateRepository;

    @InjectMocks
    private TemplateService templateService;

    private Template testTemplate;

    @BeforeEach
    void setUp() {
        testTemplate = new Template();
        testTemplate.setId("tmpl-id-1");
        testTemplate.setName("Welcome");
        testTemplate.setSubject("Hello {{name}}");
        testTemplate.setBody("<p>Welcome {{name}}!</p>");
        testTemplate.setUserId("user-id-1");
    }

    @Test
    void testGetTemplates() {
        when(templateRepository.findByUserId("user-id-1")).thenReturn(Arrays.asList(testTemplate));

        List<Template> result = templateService.getTemplates("user-id-1");

        assertEquals(1, result.size());
        assertEquals("Welcome", result.get(0).getName());
    }

    @Test
    void testGetTemplatesEmpty() {
        when(templateRepository.findByUserId("user-id-1")).thenReturn(Arrays.asList());

        List<Template> result = templateService.getTemplates("user-id-1");

        assertTrue(result.isEmpty());
    }

    @Test
    void testCreateTemplate() {
        TemplateRequest req = new TemplateRequest();
        req.setName("New Template");
        req.setSubject("New Subject");
        req.setBody("<p>New Body</p>");

        when(templateRepository.save(any(Template.class))).thenAnswer(inv -> {
            Template t = inv.getArgument(0);
            t.setId("new-tmpl-id");
            return t;
        });

        Template result = templateService.createTemplate("user-id-1", req);

        assertNotNull(result);
        assertEquals("New Template", result.getName());
        assertEquals("New Subject", result.getSubject());
        assertEquals("<p>New Body</p>", result.getBody());
        assertEquals("user-id-1", result.getUserId());
        verify(templateRepository).save(any(Template.class));
    }

    @Test
    void testUpdateTemplateSuccess() {
        when(templateRepository.findById("tmpl-id-1")).thenReturn(Optional.of(testTemplate));
        when(templateRepository.save(any(Template.class))).thenReturn(testTemplate);

        TemplateRequest req = new TemplateRequest();
        req.setName("Updated Name");
        req.setSubject("Updated Subject");
        req.setBody("<p>Updated Body</p>");

        Template result = templateService.updateTemplate("tmpl-id-1", "user-id-1", req);

        assertEquals("Updated Name", result.getName());
        assertEquals("Updated Subject", result.getSubject());
    }

    @Test
    void testUpdateTemplateNotFound() {
        when(templateRepository.findById("nonexistent")).thenReturn(Optional.empty());

        TemplateRequest req = new TemplateRequest();
        req.setName("Test");

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> templateService.updateTemplate("nonexistent", "user-id-1", req));
        assertEquals("Template not found", ex.getMessage());
    }

    @Test
    void testUpdateTemplateUnauthorized() {
        when(templateRepository.findById("tmpl-id-1")).thenReturn(Optional.of(testTemplate));

        TemplateRequest req = new TemplateRequest();
        req.setName("Test");

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> templateService.updateTemplate("tmpl-id-1", "wrong-user", req));
        assertEquals("Unauthorized", ex.getMessage());
    }

    @Test
    void testDeleteTemplateSuccess() {
        when(templateRepository.findById("tmpl-id-1")).thenReturn(Optional.of(testTemplate));

        templateService.deleteTemplate("tmpl-id-1", "user-id-1");

        verify(templateRepository).delete(testTemplate);
    }

    @Test
    void testDeleteTemplateNotFound() {
        when(templateRepository.findById("nonexistent")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> templateService.deleteTemplate("nonexistent", "user-id-1"));
        assertEquals("Template not found", ex.getMessage());
    }

    @Test
    void testDeleteTemplateUnauthorized() {
        when(templateRepository.findById("tmpl-id-1")).thenReturn(Optional.of(testTemplate));

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> templateService.deleteTemplate("tmpl-id-1", "wrong-user"));
        assertEquals("Unauthorized", ex.getMessage());
    }

    @Test
    void testGetMultipleTemplates() {
        Template t2 = new Template();
        t2.setId("tmpl-id-2");
        t2.setName("Newsletter");
        t2.setUserId("user-id-1");

        when(templateRepository.findByUserId("user-id-1")).thenReturn(Arrays.asList(testTemplate, t2));

        List<Template> result = templateService.getTemplates("user-id-1");

        assertEquals(2, result.size());
    }

    @Test
    void testCreateTemplatePreservesUserId() {
        TemplateRequest req = new TemplateRequest();
        req.setName("Test");
        req.setSubject("Subj");
        req.setBody("Body");

        when(templateRepository.save(any(Template.class))).thenAnswer(inv -> {
            Template t = inv.getArgument(0);
            t.setId("new-id");
            return t;
        });

        Template result = templateService.createTemplate("user-xyz", req);
        assertEquals("user-xyz", result.getUserId());
    }
}
