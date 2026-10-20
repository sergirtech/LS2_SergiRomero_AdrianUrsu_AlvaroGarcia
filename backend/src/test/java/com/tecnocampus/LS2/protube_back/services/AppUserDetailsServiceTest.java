package com.tecnocampus.LS2.protube_back.services;

import com.tecnocampus.LS2.protube_back.model.User;
import com.tecnocampus.LS2.protube_back.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppUserDetailsServiceTest {

    @Mock UserRepository userRepository;
    @InjectMocks AppUserDetailsService appUserDetailsService;

    @Test
    void loadsUserByNormalizedEmailWithHashAndRole() {
        User user = new User();
        user.setEmail("sergi@mail.com");
        user.setPasswordHash("hashed");
        when(userRepository.findByEmail("sergi@mail.com")).thenReturn(Optional.of(user));

        UserDetails details = appUserDetailsService.loadUserByUsername(" Sergi@Mail.com ");

        assertEquals("sergi@mail.com", details.getUsername());
        assertEquals("hashed", details.getPassword());
        assertTrue(details.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void throwsWhenUserIsMissing() {
        when(userRepository.findByEmail("x@y.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> appUserDetailsService.loadUserByUsername("x@y.com"));
    }
}
