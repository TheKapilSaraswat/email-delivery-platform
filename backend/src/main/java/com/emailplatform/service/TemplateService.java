package com.emailplatform.service;

import com.emailplatform.dto.TemplateRequest;
import com.emailplatform.model.Template;
import com.emailplatform.repository.TemplateRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TemplateService {

    private final TemplateRepository templateRepository;

    public TemplateService(TemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    public List<Template> getTemplates(String userId) {
        return templateRepository.findByUserId(userId);
    }

    public Template createTemplate(String userId, TemplateRequest request) {
        Template template = new Template();
        template.setName(request.getName());
        template.setSubject(request.getSubject());
        template.setBody(request.getBody());
        template.setUserId(userId);
        return templateRepository.save(template);
    }

    public Template updateTemplate(String id, String userId, TemplateRequest request) {
        Template template = templateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Template not found"));
        if (!template.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        template.setName(request.getName());
        template.setSubject(request.getSubject());
        template.setBody(request.getBody());
        return templateRepository.save(template);
    }

    public void deleteTemplate(String id, String userId) {
        Template template = templateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Template not found"));
        if (!template.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        templateRepository.delete(template);
    }
}
