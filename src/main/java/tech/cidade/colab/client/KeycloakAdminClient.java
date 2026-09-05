package tech.cidade.colab.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import tech.cidade.colab.config.KeycloakAdminFeignConfig;
import tech.cidade.colab.dto.request.KeycloakCreateUserRequest;

@FeignClient(name = "keycloak-admin", url = "${keycloak.base-url}", configuration = KeycloakAdminFeignConfig.class)
public interface KeycloakAdminClient {

    @PostMapping(path = "/admin/realms/${keycloak.realm}/users", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> createUser(@RequestBody KeycloakCreateUserRequest request);
}
