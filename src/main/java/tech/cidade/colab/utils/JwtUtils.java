package tech.cidade.colab.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

public final class JwtUtils {

    private JwtUtils() {
    }

    public static String getUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new IllegalStateException("Usuário não autenticado ou token JWT inválido");
        }

        String userIdClaim = jwt.getClaimAsString("userId");
        if (userIdClaim != null && !userIdClaim.isBlank()) {
            return userIdClaim;
        }

        String subject = jwt.getSubject();
        if (subject == null || subject.isBlank()) {
            throw new IllegalStateException("Token JWT sem claim 'sub'");
        }

        return subject;
    }

    public static String getUserId(Jwt jwt) {
        if (jwt == null) {
            throw new IllegalArgumentException("JWT não pode ser nulo");
        }

        String userIdClaim = jwt.getClaimAsString("userId");
        if (userIdClaim != null && !userIdClaim.isBlank()) {
            return userIdClaim;
        }

        String subject = jwt.getSubject();
        if (subject == null || subject.isBlank()) {
            throw new IllegalStateException("Token JWT sem claim 'sub'");
        }

        return subject;
    }
}