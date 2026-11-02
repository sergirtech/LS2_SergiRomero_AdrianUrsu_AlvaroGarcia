package com.tecnocampus.LS2.protube_back.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class UserTest {

    @Test
    void normalizeEmailTrimsAndLowercases() {
        assertEquals("sergi@mail.com", User.normalizeEmail("  Sergi@MAIL.com "));
    }

    @Test
    void registerCreatesUnsavedUserWithNormalizedFields() {
        User user = User.register(" Sergi@Mail.com ", " sergi ", "hashed");

        assertNull(user.getId());
        assertEquals("sergi@mail.com", user.getEmail());
        assertEquals("sergi", user.getUsername());
        assertEquals("hashed", user.getPasswordHash());
    }
}
