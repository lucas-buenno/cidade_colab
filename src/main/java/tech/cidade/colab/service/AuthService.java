package tech.cidade.colab.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tech.cidade.colab.auth.dto.keycloak.KeycloakAuthResponse;
import tech.cidade.colab.auth.dto.login.LoginRequest;
import tech.cidade.colab.auth.dto.login.LoginResponse;


@Service
@RequiredArgsConstructor
public class AuthService {

    private final KeycloakAuthService keycloakAuthService;

    public LoginResponse login(LoginRequest request) {

        KeycloakAuthResponse tokenResponse = keycloakAuthService.getUserToken(request.username(), request.password());
        return new LoginResponse(tokenResponse.accessToken());
    }
}
