package com.tecnocampus.LS2.protube_back.controller.dto;

import com.tecnocampus.LS2.protube_back.model.User;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class RegisterRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private Set<String> messages(Object request) {
        return validator.validate(request).stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());
    }

    @Test
    void validRequestHasNoErrors() {
        assertTrue(messages(new RegisterRequest("sergi@mail.com", "sergi", "password123")).isEmpty());
    }

    @Test
    void rejectsInvalidEmail() {
        assertEquals(Set.of("Enter a valid email address"),
                messages(new RegisterRequest("not-an-email", "sergi", "password123")));
    }

    @Test
    void rejectsShortPassword() {
        assertEquals(Set.of("Password must be between 8 and 72 characters"),
                messages(new RegisterRequest("sergi@mail.com", "sergi", "short")));
    }
    @Test
    void rejectsShortUsername() {
        assertEquals(Set.of("Username must be between 3 and 30 characters"),
                messages(new RegisterRequest("sergi@mail.com", "ab", "password123")));
    }

    @Test
    void rejectsMissingFields() {
        Set<String> messages = messages(new RegisterRequest("", null, null));

        assertTrue(messages.contains("Email is required"));
        assertTrue(messages.contains("Username is required"));
        assertTrue(messages.contains("Password is required"));
    }

    @Test
    void loginRequiresEmailAndPassword() {
        assertEquals(Set.of("Email is required", "Password is required"),
                messages(new LoginRequest(" ", "")));
    }

    @Test
    void userResponseDoesNotExposeThePasswordHash() {
        User user = new User();
        user.setId(1L);
        user.setEmail("sergi@mail.com");
        user.setUsername("sergi");
        user.setPasswordHash("hashed");

        assertEquals(new UserResponse(1L, "sergi@mail.com", "sergi"), UserResponse.from(user));
    }
}