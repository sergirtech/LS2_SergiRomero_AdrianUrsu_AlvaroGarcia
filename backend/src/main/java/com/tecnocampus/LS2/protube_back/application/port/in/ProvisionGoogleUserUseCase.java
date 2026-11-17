package com.tecnocampus.LS2.protube_back.application.port.in;

import com.tecnocampus.LS2.protube_back.domain.model.User;

public interface ProvisionGoogleUserUseCase {
    User provision(String googleSubject, String email, String displayName);
}
