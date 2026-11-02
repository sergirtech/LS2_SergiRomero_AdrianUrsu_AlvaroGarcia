package com.tecnocampus.LS2.protube_back.infrastructure.persistence;

import com.tecnocampus.LS2.protube_back.application.port.out.UserRepository;
import com.tecnocampus.LS2.protube_back.domain.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.*;

/** Tests the JPA adapter through the {@link UserRepository} port, against embedded H2. */
@DataJpaTest
@Import(UserPersistenceAdapter.class)
class UserPersistenceAdapterTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserJpaRepository jpaRepository;

    @Test
    void savesAndFindsUserByEmail() {
        User saved = userRepository.save(user("sergi@mail.com", "sergi"));

        User found = userRepository.findByEmail("sergi@mail.com").orElseThrow();

        assertNotNull(saved.getId());
        assertEquals(saved.getId(), found.getId());
        assertEquals("sergi@mail.com", found.getEmail());
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
        jpaRepository.saveAndFlush(UserJpaEntity.fromDomain(user("sergi@mail.com", "sergi")));

        assertThrows(DataIntegrityViolationException.class,
                () -> jpaRepository.saveAndFlush(UserJpaEntity.fromDomain(user("sergi@mail.com", "another"))));
    }

    private static User user(String email, String username) {
        return new User(null, email, username, "hashed");
    }
}
