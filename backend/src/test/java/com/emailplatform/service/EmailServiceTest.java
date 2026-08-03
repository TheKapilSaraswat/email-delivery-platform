package com.emailplatform.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @Test
    void testProcessTemplateSingleVariable() {
        Map<String, String> vars = new HashMap<>();
        vars.put("name", "John");
        String result = emailService.processTemplate("Hello {{name}}!", vars);
        assertEquals("Hello John!", result);
    }

    @Test
    void testProcessTemplateMultipleVariables() {
        Map<String, String> vars = new HashMap<>();
        vars.put("firstName", "John");
        vars.put("lastName", "Doe");
        String result = emailService.processTemplate("{{firstName}} {{lastName}}", vars);
        assertEquals("John Doe", result);
    }

    @Test
    void testProcessTemplateNoVariables() {
        String result = emailService.processTemplate("Hello World!", new HashMap<>());
        assertEquals("Hello World!", result);
    }

    @Test
    void testProcessTemplateUnknownVariableRemoved() {
        Map<String, String> vars = new HashMap<>();
        vars.put("name", "John");
        String result = emailService.processTemplate("Hello {{name}} {{unknown}}!", vars);
        assertEquals("Hello John !", result);
    }

    @Test
    void testProcessTemplateEmptyTemplate() {
        String result = emailService.processTemplate("", new HashMap<>());
        assertEquals("", result);
    }

    @Test
    void testProcessTemplateNullVariables() {
        assertThrows(NullPointerException.class, () -> emailService.processTemplate("Hello {{name}}", null));
    }

    @Test
    void testProcessTemplateHtmlContent() {
        Map<String, String> vars = new HashMap<>();
        vars.put("name", "John");
        String html = "<h1>Hello {{name}}</h1><p>Welcome to our service</p>";
        String result = emailService.processTemplate(html, vars);
        assertEquals("<h1>Hello John</h1><p>Welcome to our service</p>", result);
    }

    @Test
    void testProcessTemplateEmptyVariableValue() {
        Map<String, String> vars = new HashMap<>();
        vars.put("name", "");
        String result = emailService.processTemplate("Hello {{name}}!", vars);
        assertEquals("Hello !", result);
    }

    @Test
    void testProcessTemplateRepeatedVariable() {
        Map<String, String> vars = new HashMap<>();
        vars.put("name", "John");
        String result = emailService.processTemplate("{{name}} and {{name}}", vars);
        assertEquals("John and John", result);
    }

    @Test
    void testProcessTemplateNestedBraces() {
        Map<String, String> vars = new HashMap<>();
        vars.put("name", "John");
        String result = emailService.processTemplate("Hello {{{name}}}", vars);
        assertEquals("Hello {John}", result);
    }

    @Test
    void testProcessTemplateUnicode() {
        Map<String, String> vars = new HashMap<>();
        vars.put("name", "\u00c9t\u00e9");
        String result = emailService.processTemplate("Bonjour {{name}}!", vars);
        assertEquals("Bonjour \u00c9t\u00e9!", result);
    }

    @Test
    void testProcessTemplateSpecialCharacters() {
        Map<String, String> vars = new HashMap<>();
        vars.put("name", "O'Brien & Co.");
        String result = emailService.processTemplate("Hello {{name}}!", vars);
        assertEquals("Hello O'Brien & Co.!", result);
    }
}
