package com.tecnocampus.LS2.protube_back.application.port.in;

import com.tecnocampus.LS2.protube_back.domain.model.User;

/** Input port: US-01 user registration. */
public interface RegisterUserUseCase {

    /**
     * @throws com.tecnocampus.LS2.protube_back.domain.exception.UserAlreadyExistsException if the email or
     *         username is already taken
     */
    User register(String email, String username, String password);
}
