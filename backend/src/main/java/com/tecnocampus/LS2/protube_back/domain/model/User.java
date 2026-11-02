package com.tecnocampus.LS2.protube_back.domain.model;

import java.util.Locale;

/**
 * Domain user: plain Java, no JPA or Spring. The persistence adapter maps it to the {@code app_user} table.
 */
public class User {
    private final Long id;
    private final String email;
    private final String username;
    private final String passwordHash;

    public User(Long id, String email, String username, String passwordHash) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.passwordHash = passwordHash;
    }

    /** New user that is not stored yet (no id), with the email normalized and the username trimmed. */
    public static User register(String email, String username, String passwordHash) {
        return new User(null, normalizeEmail(email), username.trim(), passwordHash);
    }

    /** Trim + lowercase with {@link Locale#ROOT} (docs/decisions.md D4). Use it wherever an email is stored or looked up. */
    public static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }
}
