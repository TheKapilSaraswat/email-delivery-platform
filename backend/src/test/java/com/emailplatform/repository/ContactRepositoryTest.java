package com.emailplatform.repository;

import com.emailplatform.model.Contact;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ContactRepositoryTest {

    @Autowired
    private ContactRepository contactRepository;

    @Test
    void testSaveAndFind() {
        Contact c = new Contact();
        c.setEmail("john@test.com");
        c.setFirstName("John");
        c.setLastName("Doe");
        c.setList("newsletter");
        c.setUserId("user-1");
        Contact saved = contactRepository.save(c);

        assertNotNull(saved.getId());
        assertTrue(contactRepository.findById(saved.getId()).isPresent());
    }

    @Test
    void testFindByUserId() {
        Contact c1 = new Contact();
        c1.setEmail("a@test.com");
        c1.setUserId("user-1");
        c1.setList("newsletter");
        contactRepository.save(c1);

        Contact c2 = new Contact();
        c2.setEmail("b@test.com");
        c2.setUserId("user-1");
        c2.setList("vip");
        contactRepository.save(c2);

        Contact c3 = new Contact();
        c3.setEmail("c@test.com");
        c3.setUserId("user-2");
        c3.setList("newsletter");
        contactRepository.save(c3);

        List<Contact> result = contactRepository.findByUserId("user-1");
        assertEquals(2, result.size());
    }

    @Test
    void testFindByUserIdAndList() {
        Contact c1 = new Contact();
        c1.setEmail("a@test.com");
        c1.setUserId("user-1");
        c1.setList("vip");
        contactRepository.save(c1);

        Contact c2 = new Contact();
        c2.setEmail("b@test.com");
        c2.setUserId("user-1");
        c2.setList("newsletter");
        contactRepository.save(c2);

        List<Contact> result = contactRepository.findByUserIdAndList("user-1", "vip");
        assertEquals(1, result.size());
        assertEquals("a@test.com", result.get(0).getEmail());
    }

    @Test
    void testFindByUserIdAndEmailContainingIgnoreCase() {
        Contact c1 = new Contact();
        c1.setEmail("john@test.com");
        c1.setUserId("user-1");
        contactRepository.save(c1);

        Contact c2 = new Contact();
        c2.setEmail("jane@test.com");
        c2.setUserId("user-1");
        contactRepository.save(c2);

        Contact c3 = new Contact();
        c3.setEmail("bob@test.com");
        c3.setUserId("user-1");
        contactRepository.save(c3);

        List<Contact> result = contactRepository.findByUserIdAndEmailContainingIgnoreCase("user-1", "john");
        assertEquals(1, result.size());
        assertEquals("john@test.com", result.get(0).getEmail());
    }

    @Test
    void testFindByUserIdAndEmailContainingIgnoreCaseCaseInsensitive() {
        Contact c1 = new Contact();
        c1.setEmail("John@Test.com");
        c1.setUserId("user-1");
        contactRepository.save(c1);

        List<Contact> result = contactRepository.findByUserIdAndEmailContainingIgnoreCase("user-1", "john");
        assertEquals(1, result.size());
    }

    @Test
    void testFindByUserIdAndListAndEmailContainingIgnoreCase() {
        Contact c1 = new Contact();
        c1.setEmail("john@vip.com");
        c1.setUserId("user-1");
        c1.setList("vip");
        contactRepository.save(c1);

        Contact c2 = new Contact();
        c2.setEmail("john@newsletter.com");
        c2.setUserId("user-1");
        c2.setList("newsletter");
        contactRepository.save(c2);

        List<Contact> result = contactRepository.findByUserIdAndListAndEmailContainingIgnoreCase("user-1", "vip", "john");
        assertEquals(1, result.size());
        assertEquals("john@vip.com", result.get(0).getEmail());
    }

    @Test
    void testDeleteContact() {
        Contact c = new Contact();
        c.setEmail("delete@test.com");
        c.setUserId("user-1");
        Contact saved = contactRepository.save(c);

        contactRepository.deleteById(saved.getId());
        assertFalse(contactRepository.findById(saved.getId()).isPresent());
    }

    @Test
    void testCount() {
        assertEquals(0, contactRepository.count());

        Contact c = new Contact();
        c.setEmail("count@test.com");
        c.setUserId("user-1");
        contactRepository.save(c);

        assertEquals(1, contactRepository.count());
    }

    @Test
    void testFindByUserIdEmpty() {
        List<Contact> result = contactRepository.findByUserId("nonexistent");
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindByUserIdAndListEmpty() {
        List<Contact> result = contactRepository.findByUserIdAndList("user-1", "nonexistent");
        assertTrue(result.isEmpty());
    }
}
