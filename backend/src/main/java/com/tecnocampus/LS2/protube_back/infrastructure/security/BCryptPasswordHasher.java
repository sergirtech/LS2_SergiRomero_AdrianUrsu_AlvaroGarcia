package com.tecnocampus.LS2.protube_back.infrastructure.security;

import com.tecnocampus.LS2.protube_back.application.port.out.PasswordHasher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Output adapter: hashes with the BCrypt {@link PasswordEncoder} bean defined in {@code SecurityConfig} (D3). */
@Component
public class BCryptPasswordHasher implements PasswordHasher {
    private final PasswordEncoder passwordEncoder;

    public BCryptPasswordHasher(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public String hash(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
}
