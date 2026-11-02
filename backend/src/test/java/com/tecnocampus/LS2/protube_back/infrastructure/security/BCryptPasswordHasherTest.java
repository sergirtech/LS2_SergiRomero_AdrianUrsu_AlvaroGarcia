package com.tecnocampus.LS2.protube_back.infrastructure.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BCryptPasswordHasherTest {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final BCryptPasswordHasher hasher = new BCryptPasswordHasher(encoder);

    @Test
    void hashIsNotThePlainPasswordAndMatchesIt() {
        String hash = hasher.hash("password123");

        assertNotEquals("password123", hash);
        assertTrue(encoder.matches("password123", hash));
    }
}
