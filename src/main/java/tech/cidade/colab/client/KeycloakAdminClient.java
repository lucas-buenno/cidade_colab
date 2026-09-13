package tech.cidade.colab.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import tech.cidade.colab.config.KeycloakAdminFeignConfig;
import tech.cidade.colab.dto.request.KeycloakCreateUserRequest;
import tech.cidade.colab.dto.request.KeycloakCredentialRepresentation;
import tech.cidade.colab.dto.response.KeycloakUserRepresentation;

import java.util.List;

@FeignClient(name = "keycloak-admin", url = "${keycloak.base-url}", configuration = KeycloakAdminFeignConfig.class)
public interface KeycloakAdminClient {

    @PostMapping(path = "/admin/realms/${keycloak.realm}/users", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> createUser(@RequestBody KeycloakCreateUserRequest request);

    @GetMapping(path = "/admin/realms/${keycloak.realm}/users")
    List<KeycloakUserRepresentation> findUsersByEmail(
            @RequestParam("email") String email,
            @RequestParam("exact") boolean exact
    );

    @PutMapping(
            path = "/admin/realms/${keycloak.realm}/users/{id}/reset-password",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    void resetPassword(@PathVariable("id") String id, @RequestBody KeycloakCredentialRepresentation credential);
}
