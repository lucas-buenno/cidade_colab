package tech.cidade.colab.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tech.cidade.colab.service.KeycloakTokenService;

@Configuration
public class KeycloakAdminFeignConfig {

    @Bean
    public RequestInterceptor keycloakAdminAuthInterceptor(KeycloakTokenService tokenService) {
        return template -> template.header("Authorization", "Bearer " + tokenService.getAccessToken());
    }
}