package tech.cidade.colab.dto.request;

public record KeycloakCredentialRepresentation(String type, String value, boolean temporary) {

    public static KeycloakCredentialRepresentation password(String value) {
        return new KeycloakCredentialRepresentation("password", value, false);
    }
}
