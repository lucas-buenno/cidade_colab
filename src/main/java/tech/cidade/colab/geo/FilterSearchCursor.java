package tech.cidade.colab.geo;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record FilterSearchCursor(Instant createdAt, String id) implements ColabSearchPageCursor {

    private static final Pattern PAYLOAD = Pattern.compile("^filter:v1:(\\d+):(.+)$");

    public static String encode(Instant createdAt, String id) {
        if (createdAt == null || id == null || id.isBlank()) {
            throw new IllegalArgumentException("Não foi possível gerar o cursor: id inválido");
        }
        String payload = "filter:v1:" + createdAt.toEpochMilli() + ":" + id;
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(payload.getBytes(StandardCharsets.UTF_8));
    }

    public static FilterSearchCursor decode(String pageToken) {
        if (pageToken == null || pageToken.isBlank()) {
            return null;
        }

        try {
            String decoded = new String(
                    Base64.getUrlDecoder().decode(pageToken),
                    StandardCharsets.UTF_8
            );
            Matcher matcher = PAYLOAD.matcher(decoded);
            if (!matcher.matches()) {
                throw new IllegalArgumentException("pageToken inválido ou corrompido");
            }
            Instant createdAt = Instant.ofEpochMilli(Long.parseLong(matcher.group(1)));
            String id = matcher.group(2);
            if (id.isBlank()) {
                throw new IllegalArgumentException("pageToken inválido ou corrompido");
            }
            return new FilterSearchCursor(createdAt, id);
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("pageToken inválido ou corrompido");
        }
    }
}
