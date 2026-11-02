package com.tecnocampus.LS2.protube_back.application.service;

import com.tecnocampus.LS2.protube_back.application.port.in.FindUserUseCase;
import com.tecnocampus.LS2.protube_back.application.port.in.RegisterUserUseCase;
import com.tecnocampus.LS2.protube_back.application.port.out.PasswordHasher;
import com.tecnocampus.LS2.protube_back.application.port.out.UserRepository;
import com.tecnocampus.LS2.protube_back.domain.exception.UserAlreadyExistsException;
import com.tecnocampus.LS2.protube_back.domain.exception.UserNotFoundException;
import com.tecnocampus.LS2.protube_back.domain.model.User;
import org.springframework.stereotype.Service;

@Service
public class AuthService implements RegisterUserUseCase, FindUserUseCase {
    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    public AuthService(UserRepository userRepository, PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    @Override
    public User register(String email, String username, String password) {
        String normalizedEmail = User.normalizeEmail(email);
        String trimmedUsername = username.trim();
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new UserAlreadyExistsException("This email is already registered");
        }
        if (userRepository.existsByUsername(trimmedUsername)) {
            throw new UserAlreadyExistsException("This username is already taken");
        }
        return userRepository.save(User.register(normalizedEmail, trimmedUsername, passwordHasher.hash(password)));
    }

    @Override
    public User getByEmail(String email) {
        return userRepository.findByEmail(User.normalizeEmail(email))
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }
}
