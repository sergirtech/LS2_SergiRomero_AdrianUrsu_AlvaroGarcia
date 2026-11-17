package com.tecnocampus.LS2.protube_back.application.service;

import com.tecnocampus.LS2.protube_back.application.port.in.ProvisionGoogleUserUseCase;
import com.tecnocampus.LS2.protube_back.application.port.out.UserRepository;
import com.tecnocampus.LS2.protube_back.domain.exception.UserAlreadyExistsException;
import com.tecnocampus.LS2.protube_back.domain.model.User;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.Locale;

@Service
public class GoogleAccountService implements ProvisionGoogleUserUseCase {
    private static final int USERNAME_LIMIT = 30;

    private final UserRepository userRepository;

    public GoogleAccountService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User provision(String googleSubject, String email, String displayName) {
        if (googleSubject == null || googleSubject.isBlank() || email == null || email.isBlank()) {
            throw new IllegalArgumentException("Google subject and verified email are required");
        }

        return userRepository.findByGoogleSubject(googleSubject)
                .orElseGet(() -> linkOrCreate(googleSubject, email, displayName));
    }

    private User linkOrCreate(String googleSubject, String email, String displayName) {
        String normalizedEmail = User.normalizeEmail(email);
        var existing = userRepository.findByEmail(normalizedEmail);
        if (existing.isPresent()) {
            User user = existing.get();
            if (user.getGoogleSubject() != null && !user.getGoogleSubject().equals(googleSubject)) {
                throw new UserAlreadyExistsException("This account is linked to another Google account");
            }
            return userRepository.save(user.withGoogleSubject(googleSubject));
        }

        String username = uniqueUsername(displayName, normalizedEmail);
        return userRepository.save(User.registerWithGoogle(normalizedEmail, username, googleSubject));
    }

    private String uniqueUsername(String displayName, String email) {
        String base = usernameSlug(displayName);
        if (base.length() < 3) {
            base = usernameSlug(email.substring(0, email.indexOf('@')));
        }
        if (base.length() < 3) {
            base = "google-user";
        }
        base = base.substring(0, Math.min(base.length(), USERNAME_LIMIT));

        if (!userRepository.existsByUsername(base)) {
            return base;
        }
        for (long suffix = 2; ; suffix++) {
            String suffixText = "-" + suffix;
            int baseLength = USERNAME_LIMIT - suffixText.length();
            if (baseLength < 1) {
                throw new UserAlreadyExistsException("Could not generate a unique username");
            }
            String candidate = base.substring(0, Math.min(base.length(), baseLength)) + suffixText;
            if (!userRepository.existsByUsername(candidate)) {
                return candidate;
            }
        }
    }

    private static String usernameSlug(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return Normalizer.normalize(value, Normalizer.Form.NFKD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
    }
}
