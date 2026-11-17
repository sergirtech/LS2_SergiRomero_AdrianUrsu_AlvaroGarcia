package com.tecnocampus.LS2.protube_back.infrastructure.security;

import com.tecnocampus.LS2.protube_back.application.port.in.ProvisionGoogleUserUseCase;
import com.tecnocampus.LS2.protube_back.domain.exception.UserAlreadyExistsException;
import com.tecnocampus.LS2.protube_back.domain.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/** Validates Google's identity claims and maps the session name to the local user's normalized email. */
@Service
public class GoogleOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    private final OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate;
    private final ProvisionGoogleUserUseCase provisionGoogleUserUseCase;

    @Autowired
    public GoogleOAuth2UserService(ProvisionGoogleUserUseCase provisionGoogleUserUseCase) {
        this(new DefaultOAuth2UserService(), provisionGoogleUserUseCase);
    }

    GoogleOAuth2UserService(OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate,
                            ProvisionGoogleUserUseCase provisionGoogleUserUseCase) {
        this.delegate = delegate;
        this.provisionGoogleUserUseCase = provisionGoogleUserUseCase;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User googleUser = delegate.loadUser(userRequest);
        Map<String, Object> googleAttributes = googleUser.getAttributes();
        String subject = stringAttribute(googleAttributes, "sub");
        String email = stringAttribute(googleAttributes, "email");
        String displayName = stringAttribute(googleAttributes, "name");
        if (subject == null || email == null || !email.contains("@")
                || !Boolean.TRUE.equals(googleAttributes.get("email_verified"))) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("invalid_google_identity"), "Google did not provide a verified email identity");
        }

        User user;
        try {
            user = provisionGoogleUserUseCase.provision(subject, email, displayName);
        } catch (UserAlreadyExistsException exception) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("google_account_link_conflict"), exception.getMessage(), exception);
        }
        Map<String, Object> sessionAttributes = new HashMap<>(googleAttributes);
        sessionAttributes.put("email", user.getEmail());
        return new DefaultOAuth2User(googleUser.getAuthorities(), sessionAttributes, "email");
    }

    private static String stringAttribute(Map<String, Object> attributes, String name) {
        Object value = attributes.get(name);
        return value instanceof String stringValue && !stringValue.isBlank() ? stringValue : null;
    }
}
