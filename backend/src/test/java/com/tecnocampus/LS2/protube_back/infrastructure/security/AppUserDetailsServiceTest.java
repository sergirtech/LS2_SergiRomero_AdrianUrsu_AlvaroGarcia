package com.tecnocampus.LS2.protube_back.infrastructure.security;

import com.tecnocampus.LS2.protube_back.application.port.in.FindUserUseCase;
import com.tecnocampus.LS2.protube_back.domain.exception.UserNotFoundException;
import com.tecnocampus.LS2.protube_back.domain.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppUserDetailsServiceTest {

    @Mock FindUserUseCase findUserUseCase;
    @InjectMocks AppUserDetailsService appUserDetailsService;

    @Test
    void loadsUserWithHashAndRole() {
        when(findUserUseCase.getByEmail(" Sergi@Mail.com "))
                .thenReturn(new User(1L, "sergi@mail.com", "sergi", "hashed"));

        UserDetails details = appUserDetailsService.loadUserByUsername(" Sergi@Mail.com ");

        assertEquals("sergi@mail.com", details.getUsername());
        assertEquals("hashed", details.getPassword());
        assertTrue(details.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void translatesMissingUserToSpringSecurityException() {
        when(findUserUseCase.getByEmail("x@y.com")).thenThrow(new UserNotFoundException("User not found"));

        assertThrows(UsernameNotFoundException.class, () -> appUserDetailsService.loadUserByUsername("x@y.com"));
    }
}
