package com.tecnocampus.LS2.protube_back.application.port.in;

import com.tecnocampus.LS2.protube_back.domain.model.User;

/** Input port: look up a registered user. */
public interface FindUserUseCase {

    /**
     * @throws com.tecnocampus.LS2.protube_back.domain.exception.UserNotFoundException if no user has that email
     */
    User getByEmail(String email);
}
