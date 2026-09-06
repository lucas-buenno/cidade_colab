package tech.cidade.colab.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Configuration
public class SecurityConfig {

    @Value("${security.authorization.resource-client-id:cidadecolab-api}")
    private String resourceClientId;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/users", "/auth").permitAll()
                        .requestMatchers(HttpMethod.GET, "/v1/feed", "/v1/categories", "/v1/colab/*").permitAll()
                        .requestMatchers(HttpMethod.POST, "/v1/colab/create")
                        .access(new WebExpressionAuthorizationManager("hasAuthority('ROLE_COLLABORATOR') and hasAuthority('PERM_colabs:create')"))
                        .requestMatchers(HttpMethod.PUT, "/v1/colab/support/*")
                        .access(new WebExpressionAuthorizationManager("hasAuthority('ROLE_COLLABORATOR') and hasAuthority('PERM_colabs:support')"))
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
                .build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(this::extractAuthorities);
        return converter;
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        Set<GrantedAuthority> authorities = new HashSet<>();

        JwtGrantedAuthoritiesConverter scopeConverter = new JwtGrantedAuthoritiesConverter();
        authorities.addAll(scopeConverter.convert(jwt));

        Object realmAccessObj = jwt.getClaim("realm_access");
        if (realmAccessObj instanceof Map<?, ?> realmAccessMap) {
            Object rolesObj = realmAccessMap.get("roles");
            if (rolesObj instanceof List<?> roles) {
                roles.stream()
                        .filter(String.class::isInstance)
                        .map(String.class::cast)
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .forEach(authorities::add);
            }
        }

        Object resourceAccessObj = jwt.getClaim("resource_access");
        if (!(resourceAccessObj instanceof Map<?, ?> resourceAccessMap)) {
            return authorities;
        }

        Object apiClientObj = resourceAccessMap.get(resourceClientId);
        if (!(apiClientObj instanceof Map<?, ?> apiClientMap)) {
            return authorities;
        }

        Object permissionsObj = apiClientMap.get("roles");
        if (permissionsObj instanceof List<?> permissions) {
            permissions.stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .map(permission -> new SimpleGrantedAuthority("PERM_" + permission))
                    .forEach(authorities::add);
        }

        return authorities;
    }
}
