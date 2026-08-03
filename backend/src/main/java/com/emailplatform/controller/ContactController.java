package com.emailplatform.controller;

import com.emailplatform.dto.ContactRequest;
import com.emailplatform.model.Contact;
import com.emailplatform.service.ContactService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/contacts")
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @GetMapping
    public ResponseEntity<List<Contact>> getContacts(@AuthenticationPrincipal String userId,
                                                      @RequestParam(required = false) String search,
                                                      @RequestParam(required = false) String list) {
        return ResponseEntity.ok(contactService.getContacts(userId, search, list));
    }

    @PostMapping
    public ResponseEntity<?> createContact(@AuthenticationPrincipal String userId,
                                            @Valid @RequestBody ContactRequest request) {
        try {
            Contact contact = contactService.createContact(userId, request);
            return ResponseEntity.ok(contact);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateContact(@AuthenticationPrincipal String userId,
                                            @PathVariable String id,
                                            @Valid @RequestBody ContactRequest request) {
        try {
            Contact contact = contactService.updateContact(id, userId, request);
            return ResponseEntity.ok(contact);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteContact(@AuthenticationPrincipal String userId,
                                            @PathVariable String id) {
        try {
            contactService.deleteContact(id, userId);
            return ResponseEntity.ok(Map.of("message", "Contact deleted"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/import")
    public ResponseEntity<?> bulkImport(@AuthenticationPrincipal String userId,
                                         @Valid @RequestBody List<ContactRequest> requests) {
        try {
            List<Contact> contacts = contactService.bulkImport(userId, requests);
            return ResponseEntity.ok(Map.of("imported", contacts.size(), "contacts", contacts));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
