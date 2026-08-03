package com.emailplatform.repository;

import com.emailplatform.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void testSaveAndFind() {
        User user = new User();
        user.setEmail("test@test.com");
        user.setPassword("password");
        user.setName("Test User");
        User saved = userRepository.save(user);

        assertNotNull(saved.getId());
        Optional<User> found = userRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("test@test.com", found.get().getEmail());
    }

    @Test
    void testFindByEmail() {
        User user = new User();
        user.setEmail("find@test.com");
        user.setPassword("password");
        user.setName("Find User");
        userRepository.save(user);

        Optional<User> found = userRepository.findByEmail("find@test.com");
        assertTrue(found.isPresent());
        assertEquals("Find User", found.get().getName());
    }

    @Test
    void testFindByEmailNotFound() {
        Optional<User> found = userRepository.findByEmail("nonexistent@test.com");
        assertFalse(found.isPresent());
    }

    @Test
    void testExistsByEmail() {
        User user = new User();
        user.setEmail("exists@test.com");
        user.setPassword("password");
        user.setName("Exists User");
        userRepository.save(user);

        assertTrue(userRepository.existsByEmail("exists@test.com"));
    }

    @Test
    void testExistsByEmailFalse() {
        assertFalse(userRepository.existsByEmail("nothere@test.com"));
    }

    @Test
    void testSaveMultipleUsers() {
        User u1 = new User();
        u1.setEmail("a@test.com");
        u1.setPassword("pass");
        u1.setName("A");
        userRepository.save(u1);

        User u2 = new User();
        u2.setEmail("b@test.com");
        u2.setPassword("pass");
        u2.setName("B");
        userRepository.save(u2);

        assertEquals(2, userRepository.findAll().size());
    }

    @Test
    void testDeleteUser() {
        User user = new User();
        user.setEmail("delete@test.com");
        user.setPassword("password");
        user.setName("Delete User");
        User saved = userRepository.save(user);

        userRepository.deleteById(saved.getId());
        assertFalse(userRepository.findById(saved.getId()).isPresent());
    }

    @Test
    void testCount() {
        assertEquals(0, userRepository.count());

        User user = new User();
        user.setEmail("count@test.com");
        user.setPassword("pass");
        user.setName("Count");
        userRepository.save(user);

        assertEquals(1, userRepository.count());
    }

    @Test
    void testFindAll() {
        User u1 = new User();
        u1.setEmail("all1@test.com");
        u1.setPassword("pass");
        u1.setName("A");
        userRepository.save(u1);

        User u2 = new User();
        u2.setEmail("all2@test.com");
        u2.setPassword("pass");
        u2.setName("B");
        userRepository.save(u2);

        assertEquals(2, userRepository.findAll().size());
    }
}
