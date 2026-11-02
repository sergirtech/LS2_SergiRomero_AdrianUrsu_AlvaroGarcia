package com.tecnocampus.LS2.protube_back.infrastructure.security;

import com.tecnocampus.LS2.protube_back.application.port.in.FindUserUseCase;
import com.tecnocampus.LS2.protube_back.domain.exception.UserNotFoundException;
import com.tecnocampus.LS2.protube_back.domain.model.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/** Adapter between Spring Security and the application: loads users by (normalized) email. */
@Service
public class AppUserDetailsService implements UserDetailsService {

    private final FindUserUseCase findUserUseCase;

    public AppUserDetailsService(FindUserUseCase findUserUseCase) {
        this.findUserUseCase = findUserUseCase;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        User user;
        try {
            user = findUserUseCase.getByEmail(email);
        } catch (UserNotFoundException ex) {
            // Spring Security turns this into a generic bad-credentials error (D11).
            throw new UsernameNotFoundException("User not found", ex);
        }
        return org.springframework.security.core.userdetails.User.withUsername(user.getEmail())
                .password(user.getPasswordHash())
                .roles("USER")
                .build();
    }
}
