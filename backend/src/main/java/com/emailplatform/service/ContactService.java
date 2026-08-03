package com.emailplatform.service;

import com.emailplatform.dto.ContactRequest;
import com.emailplatform.model.Contact;
import com.emailplatform.repository.ContactRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContactService {

    private final ContactRepository contactRepository;

    public ContactService(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    public List<Contact> getContacts(String userId, String search, String list) {
        if (search != null && !search.isEmpty() && list != null && !list.isEmpty()) {
            return contactRepository.findByUserIdAndListAndEmailContainingIgnoreCase(userId, list, search);
        }
        if (list != null && !list.isEmpty()) {
            return contactRepository.findByUserIdAndList(userId, list);
        }
        if (search != null && !search.isEmpty()) {
            return contactRepository.findByUserIdAndEmailContainingIgnoreCase(userId, search);
        }
        return contactRepository.findByUserId(userId);
    }

    public Contact createContact(String userId, ContactRequest request) {
        Contact contact = new Contact();
        contact.setEmail(request.getEmail());
        contact.setFirstName(request.getFirstName());
        contact.setLastName(request.getLastName());
        contact.setList(request.getList());
        contact.setMetadata(request.getMetadata());
        contact.setUserId(userId);
        return contactRepository.save(contact);
    }

    public Contact updateContact(String id, String userId, ContactRequest request) {
        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contact not found"));
        if (!contact.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        contact.setEmail(request.getEmail());
        contact.setFirstName(request.getFirstName());
        contact.setLastName(request.getLastName());
        contact.setList(request.getList());
        contact.setMetadata(request.getMetadata());
        return contactRepository.save(contact);
    }

    public void deleteContact(String id, String userId) {
        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contact not found"));
        if (!contact.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        contactRepository.delete(contact);
    }

    public List<Contact> bulkImport(String userId, List<ContactRequest> requests) {
        List<Contact> contacts = requests.stream().map(req -> {
            Contact contact = new Contact();
            contact.setEmail(req.getEmail());
            contact.setFirstName(req.getFirstName());
            contact.setLastName(req.getLastName());
            contact.setList(req.getList());
            contact.setMetadata(req.getMetadata());
            contact.setUserId(userId);
            return contact;
        }).toList();
        return contactRepository.saveAll(contacts);
    }
}
