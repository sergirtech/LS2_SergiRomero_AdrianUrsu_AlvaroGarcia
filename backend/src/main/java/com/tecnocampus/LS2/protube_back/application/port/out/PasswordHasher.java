package com.tecnocampus.LS2.protube_back.application.port.out;

/** Output port: one-way password hashing. Implemented with BCrypt in {@code infrastructure/security}. */
public interface PasswordHasher {

    String hash(String rawPassword);
}
