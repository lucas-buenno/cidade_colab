package tech.cidade.colab.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import tech.cidade.colab.auth.dto.keycloak.KeycloakAuthResponse;
import tech.cidade.colab.config.DefaultFeignConfig;

import java.util.Map;

@FeignClient(name = "keycloak-auth", url = "${keycloak.base-url}", configuration = DefaultFeignConfig.class)
public interface KeycloakClient {

    @PostMapping(path = "/realms/${keycloak.realm}/protocol/openid-connect/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    KeycloakAuthResponse getToken(@RequestBody Map<String, ?> params);


    @PostMapping(path = "/realms/${keycloak.realm}/protocol/openid-connect/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    KeycloakAuthResponse getUserToken(@RequestBody Map<String, ?> authParams);
}
