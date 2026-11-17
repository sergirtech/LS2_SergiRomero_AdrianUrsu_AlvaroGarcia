package com.tecnocampus.LS2.protube_back.application.service;

import com.tecnocampus.LS2.protube_back.application.port.out.UserRepository;
import com.tecnocampus.LS2.protube_back.domain.exception.UserAlreadyExistsException;
import com.tecnocampus.LS2.protube_back.domain.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoogleAccountServiceTest {
    @Mock
    UserRepository userRepository;
    @InjectMocks
    GoogleAccountService googleAccountService;

    @Test
    void createsNormalizedAccountWithUniqueSluggedUsername() {
        when(userRepository.findByGoogleSubject("google-sub")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.empty());
        when(userRepository.existsByUsername("alice-smith")).thenReturn(true);
        when(userRepository.existsByUsername("alice-smith-2")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User user = googleAccountService.provision("google-sub", " Alice@Example.com ", "Álice Smith");

        assertEquals("alice@example.com", user.getEmail());
        assertEquals("alice-smith-2", user.getUsername());
        assertEquals("google-sub", user.getGoogleSubject());
        assertNull(user.getPasswordHash());
    }

    @Test
    void linksGoogleIdentityToExistingPasswordAccount() {
        User existing = new User(4L, "alice@example.com", "alice", "hashed-password");
        when(userRepository.findByGoogleSubject("google-sub")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User linked = googleAccountService.provision("google-sub", "alice@example.com", "New Google Name");

        assertEquals(existing.getId(), linked.getId());
        assertEquals(existing.getUsername(), linked.getUsername());
        assertEquals(existing.getPasswordHash(), linked.getPasswordHash());
        assertEquals("google-sub", linked.getGoogleSubject());
        verify(userRepository, never()).existsByUsername(any());
    }

    @Test
    void returnsAccountForPreviouslyLinkedGoogleSubject() {
        User linked = new User(4L, "alice@example.com", "alice", null, "google-sub");
        when(userRepository.findByGoogleSubject("google-sub")).thenReturn(Optional.of(linked));

        assertEquals(linked, googleAccountService.provision("google-sub", "changed@example.com", "Alice"));
        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    void refusesToReplaceAnExistingGoogleIdentityLink() {
        User linked = new User(4L, "alice@example.com", "alice", "hashed-password", "other-sub");
        when(userRepository.findByGoogleSubject("google-sub")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(linked));

        assertThrows(UserAlreadyExistsException.class,
                () -> googleAccountService.provision("google-sub", "alice@example.com", "Alice"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void requiresGoogleSubjectAndEmail() {
        assertThrows(IllegalArgumentException.class,
                () -> googleAccountService.provision(" ", "alice@example.com", "Alice"));
        assertThrows(IllegalArgumentException.class,
                () -> googleAccountService.provision("google-sub", " ", "Alice"));
    }
}
