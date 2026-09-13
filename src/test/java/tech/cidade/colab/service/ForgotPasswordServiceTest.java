package tech.cidade.colab.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.cidade.colab.auth.ForgotPasswordRateLimiter;
import tech.cidade.colab.client.KeycloakAdminClient;
import tech.cidade.colab.document.PasswordResetToken;
import tech.cidade.colab.dto.request.KeycloakCredentialRepresentation;
import tech.cidade.colab.dto.response.KeycloakUserRepresentation;
import tech.cidade.colab.exception.InvalidEmailException;
import tech.cidade.colab.exception.InvalidResetLinkException;
import tech.cidade.colab.exception.RateLimitExceededException;
import tech.cidade.colab.exception.WeakPasswordException;
import tech.cidade.colab.repository.PasswordResetTokenRepository;
import tech.cidade.colab.repository.UserRepository;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ForgotPasswordServiceTest {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");
    private static final String APP_URL = "http://localhost:5173/redefinir-senha";

    @Mock
    private KeycloakAdminClient keycloakAdminClient;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordResetTokenRepository tokenRepository;
    @Mock
    private PasswordResetMailer mailer;
    @Mock
    private ForgotPasswordRateLimiter rateLimiter;

    private ForgotPasswordService service;

    @BeforeEach
    void setUp() {
        service = new ForgotPasswordService(
                keycloakAdminClient,
                userRepository,
                tokenRepository,
                mailer,
                rateLimiter,
                APP_URL,
                900,
                Clock.fixed(NOW, ZoneOffset.UTC)
        );
    }

    @Test
    void existingAndUnknownEmailReturnTheSameMessage() {
        when(keycloakAdminClient.findUsersByEmail("voce@email.com", true))
                .thenReturn(List.of(new KeycloakUserRepresentation("kc-1")));
        String existing = service.requestReset("voce@email.com", "10.0.0.1");

        when(keycloakAdminClient.findUsersByEmail("ausente@email.com", true)).thenReturn(List.of());
        when(userRepository.existsByEmail("ausente@email.com")).thenReturn(false);
        String missing = service.requestReset("ausente@email.com", "10.0.0.1");

        assertEquals(ForgotPasswordService.GENERIC_ACCEPTED_MESSAGE, existing);
        assertEquals(existing, missing);
    }

    @Test
    void mailIsSentOnlyWhenKeycloakUserExists() {
        when(keycloakAdminClient.findUsersByEmail("voce@email.com", true))
                .thenReturn(List.of(new KeycloakUserRepresentation("kc-1")));

        service.requestReset("  Voce@email.com ", "10.0.0.1");

        verify(tokenRepository).deleteByUserIdAndUsedAtIsNull("kc-1");
        ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(tokenRepository).save(tokenCaptor.capture());
        assertEquals("kc-1", tokenCaptor.getValue().getUserId());
        assertEquals(NOW.plusSeconds(900), tokenCaptor.getValue().getExpiresAt());

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(mailer).sendResetLink(eq("voce@email.com"), urlCaptor.capture());
        assertTrue(urlCaptor.getValue().startsWith(APP_URL + "?token="));
        verify(keycloakAdminClient, never()).resetPassword(anyString(), any());
    }

    @Test
    void mailIsNotSentWhenUserIsUnknown() {
        when(keycloakAdminClient.findUsersByEmail("ausente@email.com", true)).thenReturn(List.of());
        when(userRepository.existsByEmail("ausente@email.com")).thenReturn(false);

        service.requestReset("ausente@email.com", "10.0.0.1");

        verifyNoInteractions(mailer, tokenRepository);
    }

    @Test
    void mongoOnlyUserDoesNotSendMail() {
        when(keycloakAdminClient.findUsersByEmail("voce@email.com", true)).thenReturn(List.of());
        when(userRepository.existsByEmail("voce@email.com")).thenReturn(true);

        assertEquals(ForgotPasswordService.GENERIC_ACCEPTED_MESSAGE, service.requestReset("voce@email.com", "10.0.0.1"));
        verifyNoInteractions(mailer);
    }

    @Test
    void malformedEmailDoesNotCallAdminOrMail() {
        assertThrows(InvalidEmailException.class, () -> service.requestReset("nao-e-email", "10.0.0.1"));
        verifyNoInteractions(keycloakAdminClient, userRepository, rateLimiter, mailer, tokenRepository);
    }

    @Test
    void rateLimitStopsBeforeAdminApi() {
        doThrow(new RateLimitExceededException()).when(rateLimiter).check(eq("10.0.0.1"), eq("voce@email.com"));

        assertThrows(RateLimitExceededException.class, () -> service.requestReset("voce@email.com", "10.0.0.1"));
        verifyNoInteractions(keycloakAdminClient, userRepository, mailer);
    }

    @Test
    void mailFailureStillReturnsAccepted() {
        when(keycloakAdminClient.findUsersByEmail("voce@email.com", true))
                .thenReturn(List.of(new KeycloakUserRepresentation("kc-1")));
        doThrow(new RuntimeException("smtp down")).when(mailer).sendResetLink(anyString(), anyString());

        assertEquals(ForgotPasswordService.GENERIC_ACCEPTED_MESSAGE, service.requestReset("voce@email.com", "10.0.0.1"));
    }

    @Test
    void keycloakLookupFailureStillReturnsAccepted() {
        when(keycloakAdminClient.findUsersByEmail(anyString(), anyBoolean()))
                .thenThrow(new RuntimeException("admin down"));

        assertEquals(ForgotPasswordService.GENERIC_ACCEPTED_MESSAGE, service.requestReset("voce@email.com", "10.0.0.1"));
        verifyNoInteractions(mailer);
    }

    @Test
    void resetUpdatesKeycloakAndConsumesToken() {
        PasswordResetToken stored = new PasswordResetToken()
                .setUserId("kc-1")
                .setTokenHash(ForgotPasswordRateLimiter.sha256("raw-token"))
                .setExpiresAt(NOW.plusSeconds(60));
        when(tokenRepository.findByTokenHash(ForgotPasswordRateLimiter.sha256("raw-token")))
                .thenReturn(Optional.of(stored));

        service.resetPassword("raw-token", "novasenha", "10.0.0.1");

        verify(keycloakAdminClient).resetPassword("kc-1", KeycloakCredentialRepresentation.password("novasenha"));
        assertEquals(NOW, stored.getUsedAt());
        verify(tokenRepository).save(stored);
    }

    @Test
    void resetDoesNotCallKeycloakWhenTokenUnknown() {
        when(tokenRepository.findByTokenHash(anyString())).thenReturn(Optional.empty());

        assertThrows(InvalidResetLinkException.class,
                () -> service.resetPassword("raw-token", "novasenha", "10.0.0.1"));
        verify(keycloakAdminClient, never()).resetPassword(anyString(), any());
    }

    @Test
    void resetRejectsExpiredOrUsedToken() {
        PasswordResetToken expired = new PasswordResetToken()
                .setUserId("kc-1")
                .setExpiresAt(NOW.minusSeconds(1));
        when(tokenRepository.findByTokenHash(ForgotPasswordRateLimiter.sha256("expired")))
                .thenReturn(Optional.of(expired));
        assertThrows(InvalidResetLinkException.class,
                () -> service.resetPassword("expired", "novasenha", "10.0.0.1"));

        PasswordResetToken used = new PasswordResetToken()
                .setUserId("kc-1")
                .setExpiresAt(NOW.plusSeconds(60))
                .setUsedAt(NOW.minusSeconds(10));
        when(tokenRepository.findByTokenHash(ForgotPasswordRateLimiter.sha256("used")))
                .thenReturn(Optional.of(used));
        assertThrows(InvalidResetLinkException.class,
                () -> service.resetPassword("used", "novasenha", "10.0.0.1"));

        verify(keycloakAdminClient, never()).resetPassword(anyString(), any());
    }

    @Test
    void resetRejectsWeakPasswordWithoutConsumingToken() {
        assertThrows(WeakPasswordException.class, () -> service.resetPassword("raw-token", "short", "10.0.0.1"));
        verifyNoInteractions(tokenRepository, keycloakAdminClient);
    }

    @Test
    void keycloakFailureDoesNotConsumeToken() {
        PasswordResetToken stored = new PasswordResetToken()
                .setUserId("kc-1")
                .setExpiresAt(NOW.plusSeconds(60));
        when(tokenRepository.findByTokenHash(ForgotPasswordRateLimiter.sha256("raw-token")))
                .thenReturn(Optional.of(stored));
        doThrow(new RuntimeException("kc down")).when(keycloakAdminClient)
                .resetPassword(anyString(), any());

        assertThrows(RuntimeException.class, () -> service.resetPassword("raw-token", "novasenha", "10.0.0.1"));
        verify(tokenRepository, never()).save(any());
    }
}
