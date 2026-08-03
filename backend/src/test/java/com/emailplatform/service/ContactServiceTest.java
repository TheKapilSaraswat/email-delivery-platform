package com.emailplatform.service;

import com.emailplatform.dto.ContactRequest;
import com.emailplatform.model.Contact;
import com.emailplatform.repository.ContactRepository;
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
class ContactServiceTest {

    @Mock
    private ContactRepository contactRepository;

    @InjectMocks
    private ContactService contactService;

    private Contact testContact;

    @BeforeEach
    void setUp() {
        testContact = new Contact();
        testContact.setId("contact-id-1");
        testContact.setEmail("john@test.com");
        testContact.setFirstName("John");
        testContact.setLastName("Doe");
        testContact.setList("newsletter");
        testContact.setUserId("user-id-1");
    }

    @Test
    void testGetContactsNoFilters() {
        when(contactRepository.findByUserId("user-id-1")).thenReturn(Arrays.asList(testContact));

        List<Contact> result = contactService.getContacts("user-id-1", null, null);

        assertEquals(1, result.size());
        verify(contactRepository).findByUserId("user-id-1");
    }

    @Test
    void testGetContactsWithSearch() {
        when(contactRepository.findByUserIdAndEmailContainingIgnoreCase("user-id-1", "john"))
            .thenReturn(Arrays.asList(testContact));

        List<Contact> result = contactService.getContacts("user-id-1", "john", null);

        assertEquals(1, result.size());
        verify(contactRepository).findByUserIdAndEmailContainingIgnoreCase("user-id-1", "john");
    }

    @Test
    void testGetContactsWithList() {
        when(contactRepository.findByUserIdAndList("user-id-1", "vip"))
            .thenReturn(Arrays.asList(testContact));

        List<Contact> result = contactService.getContacts("user-id-1", null, "vip");

        assertEquals(1, result.size());
        verify(contactRepository).findByUserIdAndList("user-id-1", "vip");
    }

    @Test
    void testGetContactsWithSearchAndList() {
        when(contactRepository.findByUserIdAndListAndEmailContainingIgnoreCase("user-id-1", "vip", "john"))
            .thenReturn(Arrays.asList(testContact));

        List<Contact> result = contactService.getContacts("user-id-1", "john", "vip");

        assertEquals(1, result.size());
        verify(contactRepository).findByUserIdAndListAndEmailContainingIgnoreCase("user-id-1", "vip", "john");
    }

    @Test
    void testGetContactsWithEmptySearch() {
        when(contactRepository.findByUserId("user-id-1")).thenReturn(Arrays.asList(testContact));

        List<Contact> result = contactService.getContacts("user-id-1", "", null);

        assertEquals(1, result.size());
        verify(contactRepository).findByUserId("user-id-1");
    }

    @Test
    void testGetContactsWithEmptyList() {
        when(contactRepository.findByUserId("user-id-1")).thenReturn(Arrays.asList(testContact));

        List<Contact> result = contactService.getContacts("user-id-1", null, "");

        assertEquals(1, result.size());
        verify(contactRepository).findByUserId("user-id-1");
    }

    @Test
    void testCreateContact() {
        ContactRequest req = new ContactRequest();
        req.setEmail("new@test.com");
        req.setFirstName("New");
        req.setLastName("Contact");
        req.setList("newsletter");

        when(contactRepository.save(any(Contact.class))).thenAnswer(inv -> {
            Contact c = inv.getArgument(0);
            c.setId("new-id");
            return c;
        });

        Contact result = contactService.createContact("user-id-1", req);

        assertNotNull(result);
        assertEquals("new@test.com", result.getEmail());
        assertEquals("New", result.getFirstName());
        assertEquals("user-id-1", result.getUserId());
    }

    @Test
    void testUpdateContactSuccess() {
        when(contactRepository.findById("contact-id-1")).thenReturn(Optional.of(testContact));
        when(contactRepository.save(any(Contact.class))).thenReturn(testContact);

        ContactRequest req = new ContactRequest();
        req.setEmail("updated@test.com");
        req.setFirstName("Updated");

        Contact result = contactService.updateContact("contact-id-1", "user-id-1", req);

        assertEquals("updated@test.com", result.getEmail());
    }

    @Test
    void testUpdateContactNotFound() {
        when(contactRepository.findById("nonexistent")).thenReturn(Optional.empty());

        ContactRequest req = new ContactRequest();
        req.setEmail("test@test.com");

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> contactService.updateContact("nonexistent", "user-id-1", req));
        assertEquals("Contact not found", ex.getMessage());
    }

    @Test
    void testUpdateContactUnauthorized() {
        when(contactRepository.findById("contact-id-1")).thenReturn(Optional.of(testContact));

        ContactRequest req = new ContactRequest();
        req.setEmail("test@test.com");

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> contactService.updateContact("contact-id-1", "wrong-user", req));
        assertEquals("Unauthorized", ex.getMessage());
    }

    @Test
    void testDeleteContactSuccess() {
        when(contactRepository.findById("contact-id-1")).thenReturn(Optional.of(testContact));

        contactService.deleteContact("contact-id-1", "user-id-1");

        verify(contactRepository).delete(testContact);
    }

    @Test
    void testDeleteContactNotFound() {
        when(contactRepository.findById("nonexistent")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> contactService.deleteContact("nonexistent", "user-id-1"));
        assertEquals("Contact not found", ex.getMessage());
    }

    @Test
    void testDeleteContactUnauthorized() {
        when(contactRepository.findById("contact-id-1")).thenReturn(Optional.of(testContact));

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> contactService.deleteContact("contact-id-1", "wrong-user"));
        assertEquals("Unauthorized", ex.getMessage());
    }

    @Test
    void testBulkImport() {
        ContactRequest req1 = new ContactRequest();
        req1.setEmail("a@test.com");
        req1.setFirstName("Alice");
        ContactRequest req2 = new ContactRequest();
        req2.setEmail("b@test.com");
        req2.setFirstName("Bob");

        when(contactRepository.saveAll(anyList())).thenAnswer(inv -> {
            List<Contact> contacts = inv.getArgument(0);
            contacts.get(0).setId("id-1");
            contacts.get(1).setId("id-2");
            return contacts;
        });

        List<Contact> result = contactService.bulkImport("user-id-1", Arrays.asList(req1, req2));

        assertEquals(2, result.size());
        assertEquals("a@test.com", result.get(0).getEmail());
        assertEquals("b@test.com", result.get(1).getEmail());
        verify(contactRepository).saveAll(anyList());
    }

    @Test
    void testBulkImportEmpty() {
        when(contactRepository.saveAll(anyList())).thenReturn(Arrays.asList());

        List<Contact> result = contactService.bulkImport("user-id-1", Arrays.asList());

        assertTrue(result.isEmpty());
    }

    @Test
    void testGetContactsEmpty() {
        when(contactRepository.findByUserId("user-id-1")).thenReturn(Arrays.asList());

        List<Contact> result = contactService.getContacts("user-id-1", null, null);

        assertTrue(result.isEmpty());
    }

    @Test
    void testBulkImportSetsUserId() {
        ContactRequest req = new ContactRequest();
        req.setEmail("c@test.com");

        when(contactRepository.saveAll(anyList())).thenAnswer(inv -> {
            List<Contact> contacts = inv.getArgument(0);
            contacts.get(0).setId("id-1");
            return contacts;
        });

        List<Contact> result = contactService.bulkImport("user-xyz", Arrays.asList(req));

        assertEquals("user-xyz", result.get(0).getUserId());
    }
}
