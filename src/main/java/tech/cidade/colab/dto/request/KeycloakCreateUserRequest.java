package tech.cidade.colab.dto.request;

import java.util.List;
import java.util.Map;

public record KeycloakCreateUserRequest(String username,
                                        String email,
                                        boolean enabled,
                                        List<Map<String, String>> credentials) {
}
