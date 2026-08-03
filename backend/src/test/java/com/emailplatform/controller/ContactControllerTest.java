package com.emailplatform.controller;

import com.emailplatform.dto.ContactRequest;
import com.emailplatform.model.Contact;
import com.emailplatform.service.ContactService;
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
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ContactController.class)
@AutoConfigureMockMvc(addFilters = false)
class ContactControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ContactService contactService;

    @Autowired
    private ObjectMapper objectMapper;

    private Contact createTestContact() {
        Contact c = new Contact();
        c.setId("contact-id-1");
        c.setEmail("john@test.com");
        c.setFirstName("John");
        c.setLastName("Doe");
        c.setList("newsletter");
        c.setUserId("user-id-1");
        return c;
    }

    @Test
    void testGetContacts() throws Exception {
        Contact c = createTestContact();
        when(contactService.getContacts(any(), any(), any())).thenReturn(Arrays.asList(c));

        mockMvc.perform(get("/api/contacts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("john@test.com"));
    }

    @Test
    void testGetContactsEmpty() throws Exception {
        when(contactService.getContacts(any(), any(), any())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/contacts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void testGetContactsWithSearch() throws Exception {
        Contact c = createTestContact();
        when(contactService.getContacts(any(), any(), any())).thenReturn(Arrays.asList(c));

        mockMvc.perform(get("/api/contacts").param("search", "john"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("john@test.com"));
    }

    @Test
    void testGetContactsWithList() throws Exception {
        Contact c = createTestContact();
        when(contactService.getContacts(any(), any(), any())).thenReturn(Arrays.asList(c));

        mockMvc.perform(get("/api/contacts").param("list", "vip"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("john@test.com"));
    }

    @Test
    void testCreateContact() throws Exception {
        ContactRequest req = new ContactRequest();
        req.setEmail("new@test.com");
        req.setFirstName("New");
        req.setLastName("Contact");

        Contact c = createTestContact();
        c.setEmail("new@test.com");
        when(contactService.createContact(any(), any(ContactRequest.class))).thenReturn(c);

        mockMvc.perform(post("/api/contacts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("new@test.com"));
    }

    @Test
    void testCreateContactInvalidEmail() throws Exception {
        mockMvc.perform(post("/api/contacts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"bad\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateContactFailure() throws Exception {
        ContactRequest req = new ContactRequest();
        req.setEmail("test@test.com");

        when(contactService.createContact(any(), any(ContactRequest.class)))
            .thenThrow(new RuntimeException("Error"));

        mockMvc.perform(post("/api/contacts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateContact() throws Exception {
        ContactRequest req = new ContactRequest();
        req.setEmail("updated@test.com");
        req.setFirstName("Updated");

        Contact c = createTestContact();
        c.setEmail("updated@test.com");
        when(contactService.updateContact(eq("contact-id-1"), any(), any(ContactRequest.class))).thenReturn(c);

        mockMvc.perform(put("/api/contacts/contact-id-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("updated@test.com"));
    }

    @Test
    void testUpdateContactNotFound() throws Exception {
        ContactRequest req = new ContactRequest();
        req.setEmail("test@test.com");

        when(contactService.updateContact(eq("nonexistent"), any(), any(ContactRequest.class)))
            .thenThrow(new RuntimeException("Contact not found"));

        mockMvc.perform(put("/api/contacts/nonexistent")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Contact not found"));
    }

    @Test
    void testDeleteContact() throws Exception {
        doNothing().when(contactService).deleteContact(any(), any());

        mockMvc.perform(delete("/api/contacts/contact-id-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Contact deleted"));
    }

    @Test
    void testBulkImport() throws Exception {
        ContactRequest req1 = new ContactRequest();
        req1.setEmail("a@test.com");
        ContactRequest req2 = new ContactRequest();
        req2.setEmail("b@test.com");

        Contact c1 = createTestContact();
        c1.setId("c1");
        c1.setEmail("a@test.com");
        Contact c2 = createTestContact();
        c2.setId("c2");
        c2.setEmail("b@test.com");

        when(contactService.bulkImport(any(), any())).thenReturn(Arrays.asList(c1, c2));

        mockMvc.perform(post("/api/contacts/import")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Arrays.asList(req1, req2))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imported").value(2));
    }

    @Test
    void testBulkImportEmpty() throws Exception {
        when(contactService.bulkImport(any(), any())).thenReturn(Collections.emptyList());

        mockMvc.perform(post("/api/contacts/import")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imported").value(0));
    }
}
