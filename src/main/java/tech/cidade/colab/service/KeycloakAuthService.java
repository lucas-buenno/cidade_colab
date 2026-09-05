package tech.cidade.colab.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import tech.cidade.colab.client.KeycloakAdminClient;
import tech.cidade.colab.client.KeycloakClient;
import tech.cidade.colab.auth.dto.keycloak.KeycloakAuthResponse;
import tech.cidade.colab.dto.request.KeycloakCreateUserRequest;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class KeycloakAuthService {


    @Value("${keycloak.auth.client-id}")
    private String clientId;

    @Value("${keycloak.auth.client-secret}")
    private String clientSecret;

    @Value("${keycloak.auth.grant-type}")
    private String grantType;

    private final KeycloakClient keycloakAuthClient;
    private final KeycloakAdminClient keycloakAdminClient;


    protected KeycloakAuthResponse getUserToken(String username, String password) {

        try {

            Map<String, Object> authParams = new HashMap<>();
            authParams.put("grant_type", grantType);
            authParams.put("client_id", clientId);
            authParams.put("client_secret", clientSecret);
            authParams.put("username", username);
            authParams.put("password", password);
            return keycloakAuthClient.getUserToken(authParams);

        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to get user token from Keycloak", e);
        }
    }

    public String create(String username, String email, String password) {
        try {
            ResponseEntity<Void> response = keycloakAdminClient.createUser(new KeycloakCreateUserRequest(
                    username,
                    email,
                    true,
                    List.of(Map.of(
                            "type", "password",
                            "value", password,
                            "temporary", "false")
                    )));

            String location = response.getHeaders().getFirst("Location");
            if (location == null || location.isBlank()) {
                throw new IllegalStateException("Keycloak did not return Location header");
            }

            String path = URI.create(location).getPath();
            return path.substring(path.lastIndexOf('/') + 1);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to create user on Keycloak", e);
        }
    }
}
