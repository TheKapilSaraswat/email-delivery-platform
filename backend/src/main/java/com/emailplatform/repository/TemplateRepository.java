package com.emailplatform.repository;

import com.emailplatform.model.Template;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TemplateRepository extends JpaRepository<Template, String> {
    List<Template> findByUserId(String userId);
}
