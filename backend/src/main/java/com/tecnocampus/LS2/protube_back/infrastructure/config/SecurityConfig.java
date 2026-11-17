package com.tecnocampus.LS2.protube_back.infrastructure.config;

import com.tecnocampus.LS2.protube_back.infrastructure.security.GoogleOAuth2UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final GoogleOAuth2UserService googleOAuth2UserService;
    private final ObjectProvider<ClientRegistrationRepository> clientRegistrationRepositories;

    @Value("${pro_tube.frontend.url:/}")
    private String frontendUrl;

    public SecurityConfig(
            GoogleOAuth2UserService googleOAuth2UserService,
            ObjectProvider<ClientRegistrationRepository> clientRegistrationRepositories) {
        this.googleOAuth2UserService = googleOAuth2UserService;
        this.clientRegistrationRepositories = clientRegistrationRepositories;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // MVP decision (docs/decisions.md D13): CSRF disabled, known risk with cookie sessions.
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                // Lets the H2 console (dev only) render its frames.
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(antMatcher(HttpMethod.POST, "/api/auth/register"),
                                antMatcher(HttpMethod.POST, "/api/auth/login")).permitAll()
                        .requestMatchers(antMatcher(HttpMethod.GET, "/api/videos/mine")).authenticated()
                        .requestMatchers(antMatcher(HttpMethod.GET, "/api/videos/**"),
                                antMatcher(HttpMethod.GET, "/api/hello")).permitAll()
                        .requestMatchers(antMatcher("/api/**")).authenticated()
                        .anyRequest().permitAll())
                // Answer 401 instead of redirecting to a login form the SPA doesn't have.
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(HttpStatus.NO_CONTENT)));
        if (clientRegistrationRepositories.getIfAvailable() != null) {
            http.oauth2Login(oauth2 -> oauth2
                    .userInfoEndpoint(userInfo -> userInfo.userService(googleOAuth2UserService))
                    .defaultSuccessUrl(frontendUrl, true)
                    .failureUrl(frontendLoginUrl()));
        }
        return http.build();
    }

    private String frontendLoginUrl() {
        String baseUrl = frontendUrl.endsWith("/")
                ? frontendUrl.substring(0, frontendUrl.length() - 1)
                : frontendUrl;
        return baseUrl + "/login?oauthError";
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
