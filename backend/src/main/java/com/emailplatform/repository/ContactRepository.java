package com.emailplatform.repository;

import com.emailplatform.model.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ContactRepository extends JpaRepository<Contact, String> {
    List<Contact> findByUserId(String userId);
    List<Contact> findByUserIdAndList(String userId, String list);
    List<Contact> findByUserIdAndEmailContainingIgnoreCase(String userId, String email);
    List<Contact> findByUserIdAndListAndEmailContainingIgnoreCase(String userId, String list, String email);
    List<Contact> findByIdIn(List<String> ids);
}
