package com.tecnocampus.LS2.protube_back.repository;

import com.tecnocampus.LS2.protube_back.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    UserRepository userRepository;

    @Test
    void savesAndFindsUserByEmail() {
        User saved = userRepository.save(user("sergi@mail.com", "sergi"));

        User found = userRepository.findByEmail("sergi@mail.com").orElseThrow();

        assertNotNull(saved.getId());
        assertEquals(saved.getId(), found.getId());
        assertEquals("sergi", found.getUsername());
        assertEquals("hashed", found.getPasswordHash());
    }

    @Test
    void findByEmailIsEmptyWhenUserDoesNotExist() {
        assertTrue(userRepository.findByEmail("nobody@mail.com").isEmpty());
    }

    @Test
    void existsChecksEmailAndUsername() {
        userRepository.save(user("sergi@mail.com", "sergi"));

        assertTrue(userRepository.existsByEmail("sergi@mail.com"));
        assertFalse(userRepository.existsByEmail("other@mail.com"));
        assertTrue(userRepository.existsByUsername("sergi"));
        assertFalse(userRepository.existsByUsername("other"));
    }

    @Test
    void databaseRejectsDuplicateEmail() {
        userRepository.saveAndFlush(user("sergi@mail.com", "sergi"));

        assertThrows(DataIntegrityViolationException.class,
                () -> userRepository.saveAndFlush(user("sergi@mail.com", "another")));
    }

    private static User user(String email, String username) {
        User user = new User();
        user.setEmail(email);
        user.setUsername(username);
        user.setPasswordHash("hashed");
        return user;
    }
}