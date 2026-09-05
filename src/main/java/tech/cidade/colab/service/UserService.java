package tech.cidade.colab.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.cidade.colab.repository.UserRepository;
import tech.cidade.colab.dto.request.CreateUserRequest;
import tech.cidade.colab.document.User;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final KeycloakAuthService keycloakService;
    private final UserRepository userRepository;

    @Transactional
    public String createUser(CreateUserRequest request) {
        log.info("Criando usuário com username: {} e email: {}", request.username(), request.email());
        validateUserData(request);

        log.info("Criando usuário no Keycloak com username: {}", request.username());
        String userId = keycloakService.create(request.username(), request.email(), request.password());

        log.info("Usuário criado com sucesso no Keycloak com userId: {} - Salvando no banco de dados", userId);
        User user = User.create(userId, request.username(), request.email());

        userRepository.save(user);
        log.info("Usuário criado com sucesso no banco de dados com username {} e id {}", request.username(), userId);
        return user.getId();
    }

    private void validateUserData(CreateUserRequest request) {

        if (request.username().isBlank() || request.email().isBlank() || request.password().isBlank()) {
            //TODO - Create a custom exception for this case
            log.error("Usuário não pode ser criado com dados em branco");
            throw new IllegalArgumentException("Username, email and password must not be blank");
        }

        if (userRepository.existsByUsername(request.username())) {
            log.error("Já existe um usuário cadastrado com o username informado: {}", request.username());
            throw new IllegalArgumentException("Já existe um usuário cadastrado com o username informado");
        }

        if (userRepository.existsByEmail(request.email())) {
            log.error("Já existe um usuário cadastrado com o email informado: {}", request.email());
            throw new IllegalArgumentException("Já existe um usuário cadastrado com o email informado");
        }
        log.info("Dados do usuário validados com sucesso para username- {}", request.username());
    }
}
