package tech.cidade.colab.geo;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record GeoSearchCursor(long distanceMeters, String id) implements ColabSearchPageCursor {

    private static final Pattern PAYLOAD = Pattern.compile("^geo:v1:(\\d+):(.+)$");

    public static String encode(long distanceMeters, String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Não foi possível gerar o cursor: id inválido");
        }
        String payload = "geo:v1:" + distanceMeters + ":" + id;
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(payload.getBytes(StandardCharsets.UTF_8));
    }

    public static GeoSearchCursor decode(String pageToken) {
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
            long distanceMeters = Long.parseLong(matcher.group(1));
            String id = matcher.group(2);
            if (id.isBlank()) {
                throw new IllegalArgumentException("pageToken inválido ou corrompido");
            }
            return new GeoSearchCursor(distanceMeters, id);
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("pageToken inválido ou corrompido");
        }
    }
}
