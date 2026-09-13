package tech.cidade.colab.auth.dto.login;

public record ResetPasswordRequest(String token, String password) {
}
