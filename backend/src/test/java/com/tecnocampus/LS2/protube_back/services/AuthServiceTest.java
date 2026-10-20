package com.tecnocampus.LS2.protube_back.services;

import com.tecnocampus.LS2.protube_back.model.User;
import com.tecnocampus.LS2.protube_back.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)

public class AuthServiceTest {
    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @InjectMocks AuthService authService;

    @Test
    void registerSavesUserWithNormalizedEmailAndHashedPassword() {
        when(passwordEncoder.encode("password123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User user = authService.register(" Sergi@Mail.com ", " sergi ", "password123");

        assertEquals("sergi@mail.com", user.getEmail());
        assertEquals("sergi", user.getUsername());
        assertEquals("hashed", user.getPasswordHash());
    }

    @Test
    void registerRejectsDuplicateEmail() {
        when(userRepository.existsByEmail("sergi@mail.com")).thenReturn(true);

        UserAlreadyExistsException ex = assertThrows(UserAlreadyExistsException.class,
                () -> authService.register("Sergi@mail.com", "sergi", "password123"));

        assertEquals("This email is already registered", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerRejectsDuplicateUsername() {
        when(userRepository.existsByUsername("sergi")).thenReturn(true);

        UserAlreadyExistsException ex = assertThrows(UserAlreadyExistsException.class,
                () -> authService.register("sergi@mail.com", "sergi", "password123"));

        assertEquals("This username is already taken", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void getByEmailNormalizesAndReturnsUser() {
        User user = new User();
        when(userRepository.findByEmail("sergi@mail.com")).thenReturn(Optional.of(user));

        assertSame(user, authService.getByEmail(" Sergi@Mail.com "));
    }

    @Test
    void getByEmailThrowsWhenUserIsMissing() {
        when(userRepository.findByEmail("x@y.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> authService.getByEmail("x@y.com"));
    }
}
