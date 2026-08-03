package com.emailplatform.repository;

import com.emailplatform.model.Template;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class TemplateRepositoryTest {

    @Autowired
    private TemplateRepository templateRepository;

    @Test
    void testSaveAndFind() {
        Template t = new Template();
        t.setName("Welcome");
        t.setSubject("Hello");
        t.setBody("<p>Hi</p>");
        t.setUserId("user-1");
        Template saved = templateRepository.save(t);

        assertNotNull(saved.getId());
        assertTrue(templateRepository.findById(saved.getId()).isPresent());
    }

    @Test
    void testFindByUserId() {
        Template t1 = new Template();
        t1.setName("T1");
        t1.setSubject("S1");
        t1.setBody("B1");
        t1.setUserId("user-1");
        templateRepository.save(t1);

        Template t2 = new Template();
        t2.setName("T2");
        t2.setSubject("S2");
        t2.setBody("B2");
        t2.setUserId("user-1");
        templateRepository.save(t2);

        Template t3 = new Template();
        t3.setName("T3");
        t3.setSubject("S3");
        t3.setBody("B3");
        t3.setUserId("user-2");
        templateRepository.save(t3);

        List<Template> result = templateRepository.findByUserId("user-1");
        assertEquals(2, result.size());
    }

    @Test
    void testFindByUserIdEmpty() {
        List<Template> result = templateRepository.findByUserId("nonexistent");
        assertTrue(result.isEmpty());
    }

    @Test
    void testDeleteTemplate() {
        Template t = new Template();
        t.setName("To Delete");
        t.setSubject("Subj");
        t.setBody("Body");
        t.setUserId("user-1");
        Template saved = templateRepository.save(t);

        templateRepository.deleteById(saved.getId());
        assertFalse(templateRepository.findById(saved.getId()).isPresent());
    }

    @Test
    void testCount() {
        assertEquals(0, templateRepository.count());

        Template t = new Template();
        t.setName("Count");
        t.setSubject("Subj");
        t.setBody("Body");
        t.setUserId("user-1");
        templateRepository.save(t);

        assertEquals(1, templateRepository.count());
    }

    @Test
    void testTemplateBodyLarge() {
        Template t = new Template();
        t.setName("Large");
        t.setSubject("Subj");
        t.setBody("<p>" + "x".repeat(10000) + "</p>");
        t.setUserId("user-1");
        Template saved = templateRepository.save(t);

        Template found = templateRepository.findById(saved.getId()).get();
        assertTrue(found.getBody().length() > 10000);
    }
}
