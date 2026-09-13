package tech.cidade.colab.auth;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.cidade.colab.auth.dto.login.ForgotPasswordRequest;
import tech.cidade.colab.auth.dto.login.ForgotPasswordResponse;
import tech.cidade.colab.auth.dto.login.LoginRequest;
import tech.cidade.colab.auth.dto.login.LoginResponse;
import tech.cidade.colab.auth.dto.login.ResetPasswordRequest;
import tech.cidade.colab.exception.InvalidEmailException;
import tech.cidade.colab.exception.InvalidResetLinkException;
import tech.cidade.colab.exception.RateLimitExceededException;
import tech.cidade.colab.exception.WeakPasswordException;
import tech.cidade.colab.service.AuthService;
import tech.cidade.colab.service.ForgotPasswordService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final ForgotPasswordService forgotPasswordService;

    @PostMapping
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ForgotPasswordResponse> forgotPassword(
            @RequestBody ForgotPasswordRequest request,
            HttpServletRequest httpRequest
    ) {
        String message = forgotPasswordService.requestReset(request == null ? null : request.email(), clientIp(httpRequest));
        return ResponseEntity.accepted().body(new ForgotPasswordResponse(message));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(
            @RequestBody ResetPasswordRequest request,
            HttpServletRequest httpRequest
    ) {
        String token = request == null ? null : request.token();
        String password = request == null ? null : request.password();
        forgotPasswordService.resetPassword(token, password, clientIp(httpRequest));
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(InvalidEmailException.class)
    public ResponseEntity<String> handleInvalidEmail(InvalidEmailException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler({InvalidResetLinkException.class, WeakPasswordException.class})
    public ResponseEntity<String> handleResetClientError(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<Void> handleRateLimit() {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
    }

    private static String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
