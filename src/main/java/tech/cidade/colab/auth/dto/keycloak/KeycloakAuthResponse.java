package tech.cidade.colab.auth.dto.keycloak;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.experimental.Accessors;

@Accessors(chain=true)
public record KeycloakAuthResponse(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("expires_in") int expiresIn,
        @JsonProperty("refresh_expires_in") int refreshExpiresIn,
        @JsonProperty("refresh_token") String refreshToken,
        @JsonProperty("token_type") String tokenType,
        @JsonProperty("id_token") String idToken,
        @JsonProperty("scope") String scope) {
}
