package tech.cidade.colab.utils;

import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
public final class CursorUtils {

    private CursorUtils() {
    }

    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 50;

    public static String encode(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Não foi possível gerar o cursor: id inválido");
        }

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(id.getBytes(StandardCharsets.UTF_8));
    }

    public static String decode(String pageToken) {
        if (pageToken == null || pageToken.isBlank()) {
            return null;
        }

        try {
            String decoded = new String(
                    Base64.getUrlDecoder().decode(pageToken),
                    StandardCharsets.UTF_8
            );

            if (decoded.isBlank() || !decoded.matches("^[a-fA-F0-9]{24}$")) {
                throw new IllegalArgumentException("pageToken inválido ou corrompido");
            }

            return decoded;
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("pageToken inválido ou corrompido");
        }
    }

    public static int validateAndNormalizeSize(int size) {
        if (size <= 0) {
            log.warn("Size inválido recebido: {}. Aplicando valor padrão: {}", size, DEFAULT_SIZE);
            return DEFAULT_SIZE;
        }
        if (size > MAX_SIZE) {
            log.warn("Size {} excede o máximo permitido. Aplicando limite: {}", size, MAX_SIZE);
            return MAX_SIZE;
        }
        return size;
    }
}
