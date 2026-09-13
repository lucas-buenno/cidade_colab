package tech.cidade.colab.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tech.cidade.colab.auth.ForgotPasswordRateLimiter;
import tech.cidade.colab.client.KeycloakAdminClient;
import tech.cidade.colab.document.PasswordResetToken;
import tech.cidade.colab.dto.request.KeycloakCredentialRepresentation;
import tech.cidade.colab.dto.response.KeycloakUserRepresentation;
import tech.cidade.colab.exception.InvalidEmailException;
import tech.cidade.colab.exception.InvalidResetLinkException;
import tech.cidade.colab.exception.WeakPasswordException;
import tech.cidade.colab.repository.PasswordResetTokenRepository;
import tech.cidade.colab.repository.UserRepository;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
@Slf4j
public class ForgotPasswordService {

    static final String GENERIC_ACCEPTED_MESSAGE = "Se o e-mail existir, enviaremos instruções.";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int TOKEN_BYTES = 32;

    private final KeycloakAdminClient keycloakAdminClient;
    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordResetMailer mailer;
    private final ForgotPasswordRateLimiter rateLimiter;
    private final String appResetUrl;
    private final Duration tokenTtl;
    private final Clock clock;
    private final SecureRandom secureRandom = new SecureRandom();

    @Autowired
    public ForgotPasswordService(
            KeycloakAdminClient keycloakAdminClient,
            UserRepository userRepository,
            PasswordResetTokenRepository tokenRepository,
            PasswordResetMailer mailer,
            ForgotPasswordRateLimiter rateLimiter,
            @Value("${password-reset.app-url}") String appResetUrl,
            @Value("${password-reset.ttl-seconds:900}") long ttlSeconds
    ) {
        this(
                keycloakAdminClient,
                userRepository,
                tokenRepository,
                mailer,
                rateLimiter,
                appResetUrl,
                ttlSeconds,
                Clock.systemUTC()
        );
    }

    ForgotPasswordService(
            KeycloakAdminClient keycloakAdminClient,
            UserRepository userRepository,
            PasswordResetTokenRepository tokenRepository,
            PasswordResetMailer mailer,
            ForgotPasswordRateLimiter rateLimiter,
            String appResetUrl,
            long ttlSeconds,
            Clock clock
    ) {
        this.keycloakAdminClient = keycloakAdminClient;
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.mailer = mailer;
        this.rateLimiter = rateLimiter;
        this.appResetUrl = appResetUrl;
        this.tokenTtl = Duration.ofSeconds(ttlSeconds);
        this.clock = clock;
    }

    public String requestReset(String email, String clientIp) {
        String normalizedEmail = normalize(email);
        if (!isValidEmail(normalizedEmail)) {
            throw new InvalidEmailException();
        }

        rateLimiter.check(clientIp, normalizedEmail);
        String emailHash = ForgotPasswordRateLimiter.sha256(normalizedEmail).substring(0, 12);
        log.info("forgot-password attempt emailHash={}", emailHash);

        try {
            List<KeycloakUserRepresentation> users = keycloakAdminClient.findUsersByEmail(normalizedEmail, true);
            if (users == null || users.isEmpty()) {
                if (userRepository.existsByEmail(normalizedEmail)) {
                    log.warn("forgot-password mongo user without Keycloak account emailHash={}", emailHash);
                }
                return GENERIC_ACCEPTED_MESSAGE;
            }

            String userId = users.get(0).id();
            if (userId == null || userId.isBlank()) {
                log.error("forgot-password Keycloak user without id emailHash={}", emailHash);
                return GENERIC_ACCEPTED_MESSAGE;
            }

            String rawToken = newToken();
            Instant now = clock.instant();
            tokenRepository.deleteByUserIdAndUsedAtIsNull(userId);
            tokenRepository.save(new PasswordResetToken()
                    .setTokenHash(ForgotPasswordRateLimiter.sha256(rawToken))
                    .setUserId(userId)
                    .setCreatedAt(now)
                    .setExpiresAt(now.plus(tokenTtl)));

            mailer.sendResetLink(normalizedEmail, resetLink(rawToken));
            log.info("forgot-password email dispatched emailHash={}", emailHash);
        } catch (Exception e) {
            log.error("forgot-password failed after lookup emailHash={}", emailHash, e);
        }

        return GENERIC_ACCEPTED_MESSAGE;
    }

    public void resetPassword(String rawToken, String password, String clientIp) {
        rateLimiter.checkResetIp(clientIp);
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new WeakPasswordException();
        }
        if (rawToken == null || rawToken.isBlank()) {
            throw new InvalidResetLinkException();
        }

        PasswordResetToken stored = tokenRepository.findByTokenHash(ForgotPasswordRateLimiter.sha256(rawToken.trim()))
                .orElseThrow(InvalidResetLinkException::new);

        Instant now = clock.instant();
        if (stored.getUsedAt() != null || stored.getExpiresAt() == null || !stored.getExpiresAt().isAfter(now)) {
            throw new InvalidResetLinkException();
        }

        keycloakAdminClient.resetPassword(stored.getUserId(), KeycloakCredentialRepresentation.password(password));
        stored.setUsedAt(now);
        tokenRepository.save(stored);
        log.info("password reset completed for keycloak user");
    }

    private String newToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String resetLink(String rawToken) {
        String encoded = URLEncoder.encode(rawToken, StandardCharsets.UTF_8);
        String separator = appResetUrl.contains("?") ? "&" : "?";
        return appResetUrl + separator + "token=" + encoded;
    }

    static String normalize(String email) {
        if (email == null) {
            return "";
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    static boolean isValidEmail(String email) {
        return !email.isBlank() && EMAIL_PATTERN.matcher(email).matches();
    }
}
