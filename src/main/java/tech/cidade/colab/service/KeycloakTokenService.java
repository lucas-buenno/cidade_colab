package tech.cidade.colab.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tech.cidade.colab.auth.dto.keycloak.KeycloakAuthResponse;
import tech.cidade.colab.client.KeycloakClient;

import java.time.Instant;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class KeycloakTokenService {

    private final KeycloakClient keycloakAuthClient;

    @Value("${keycloak.admin.client-id}")
    private String clientId;

    @Value("${keycloak.admin.client-secret}")
    private String clientSecret;


    private String accessToken;
    private Instant expiresAt = Instant.EPOCH;

    public synchronized String getAccessToken() {
        if (accessToken == null || Instant.now().isAfter(expiresAt.minusSeconds(30))) {
            KeycloakAuthResponse response = keycloakAuthClient.getToken(Map.of(
                    "grant_type", "client_credentials",
                    "client_id", clientId,
                    "client_secret", clientSecret
            ));
            accessToken = response.accessToken();
            expiresAt = Instant.now().plusSeconds(response.expiresIn());
        }
        return accessToken;
    }
}
