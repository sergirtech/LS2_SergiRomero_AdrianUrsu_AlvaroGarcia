package com.tecnocampus.LS2.protube_back.infrastructure.security;

import com.tecnocampus.LS2.protube_back.application.port.in.ProvisionGoogleUserUseCase;
import com.tecnocampus.LS2.protube_back.domain.exception.UserAlreadyExistsException;
import com.tecnocampus.LS2.protube_back.domain.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GoogleOAuth2UserServiceTest {
    private static final String EMAIL = "alice@example.com";
    private static final String SUBJECT = "google-sub";
    private final OAuth2UserRequest request = mock(OAuth2UserRequest.class);
    private final ProvisionGoogleUserUseCase provisionUseCase = mock(ProvisionGoogleUserUseCase.class);

    @Test
    void requiresVerifiedEmailAndUsesLocalEmailAsSessionIdentity() {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = delegate(attributes(true));
        when(provisionUseCase.provision(SUBJECT, "Alice@Example.com", "Alice Smith"))
                .thenReturn(new User(2L, EMAIL, "alice-smith", null, SUBJECT));

        OAuth2User user = new GoogleOAuth2UserService(delegate, provisionUseCase).loadUser(request);

        assertEquals(EMAIL, user.getName());
        assertEquals(EMAIL, user.getAttribute("email"));
        assertEquals("Alice Smith", user.getAttribute("name"));
        verify(provisionUseCase).provision(SUBJECT, "Alice@Example.com", "Alice Smith");
    }

    @Test
    void rejectsUnverifiedGoogleEmailBeforeProvisioning() {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = delegate(attributes(false));

        assertThrows(OAuth2AuthenticationException.class,
                () -> new GoogleOAuth2UserService(delegate, provisionUseCase).loadUser(request));
        verify(provisionUseCase, never()).provision(SUBJECT, "Alice@Example.com", "Alice Smith");
    }

    @Test
    void rejectsAccountLinkConflictsAsOAuthAuthenticationErrors() {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = delegate(attributes(true));
        when(provisionUseCase.provision(SUBJECT, "Alice@Example.com", "Alice Smith"))
                .thenThrow(new UserAlreadyExistsException("This account is linked to another Google account"));

        assertThrows(OAuth2AuthenticationException.class,
                () -> new GoogleOAuth2UserService(delegate, provisionUseCase).loadUser(request));
    }

    private OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate(Map<String, Object> attributes) {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = mock(OAuth2UserService.class);
        when(delegate.loadUser(request)).thenReturn(new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")), attributes, "sub"));
        return delegate;
    }

    private static Map<String, Object> attributes(boolean verified) {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", SUBJECT);
        attributes.put("email", "Alice@Example.com");
        attributes.put("email_verified", verified);
        attributes.put("name", "Alice Smith");
        return attributes;
    }
}
