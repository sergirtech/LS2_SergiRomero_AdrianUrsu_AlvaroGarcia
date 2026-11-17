package com.tecnocampus.LS2.protube_back.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;

import java.util.List;

@Configuration
@Conditional(GoogleOAuth2ClientConfiguration.CredentialsConfigured.class)
public class GoogleOAuth2ClientConfiguration {
    @Bean
    public ClientRegistrationRepository googleClientRegistrationRepository(
            @Value("${pro_tube.google.client-id:}") String clientId,
            @Value("${pro_tube.google.client-secret:}") String clientSecret) {
        if (clientId.isBlank() || clientSecret.isBlank()) {
            throw new IllegalStateException(
                    "Configure both ENV_PROTUBE_GOOGLE_CLIENT_ID and ENV_PROTUBE_GOOGLE_CLIENT_SECRET to enable Google sign-in.");
        }

        ClientRegistration google = ClientRegistration.withRegistrationId("google")
                .clientId(clientId)
                .clientSecret(clientSecret)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .scope(List.of("openid", "profile", "email"))
                .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                .tokenUri("https://oauth2.googleapis.com/token")
                .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
                .userNameAttributeName("sub")
                .clientName("Google")
                .issuerUri("https://accounts.google.com")
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .build();
        return new InMemoryClientRegistrationRepository(google);
    }

    static final class CredentialsConfigured implements Condition {
        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            String clientId = context.getEnvironment().getProperty("pro_tube.google.client-id", "");
            String clientSecret = context.getEnvironment().getProperty("pro_tube.google.client-secret", "");
            return !clientId.isBlank() || !clientSecret.isBlank();
        }
    }
}
