package tech.cidade.colab.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateUserRequest(
        @NotBlank String username,
        @Email String email,
        @Min(value = 8, message = "A senha deve conter ao menos 8 caracteres") String password) {
}
