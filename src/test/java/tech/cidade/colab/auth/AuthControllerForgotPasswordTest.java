package tech.cidade.colab.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tech.cidade.colab.exception.InvalidEmailException;
import tech.cidade.colab.exception.InvalidResetLinkException;
import tech.cidade.colab.exception.RateLimitExceededException;
import tech.cidade.colab.exception.WeakPasswordException;
import tech.cidade.colab.service.AuthService;
import tech.cidade.colab.service.ForgotPasswordService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerForgotPasswordTest {

    private static final String ACCEPTED_MESSAGE = "Se o e-mail existir, enviaremos instruções.";

    @Mock
    private AuthService authService;
    @Mock
    private ForgotPasswordService forgotPasswordService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(authService, forgotPasswordService)).build();
    }

    @Test
    void existingAndUnknownEmailReturnTheSameAcceptedBody() throws Exception {
        when(forgotPasswordService.requestReset(eq("voce@email.com"), any())).thenReturn(ACCEPTED_MESSAGE);

        mockMvc.perform(post("/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"voce@email.com\"}"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.message").value(ACCEPTED_MESSAGE))
                .andExpect(jsonPath("$.accessToken").doesNotExist())
                .andExpect(jsonPath("$.token").doesNotExist());

        when(forgotPasswordService.requestReset(eq("ausente@email.com"), any())).thenReturn(ACCEPTED_MESSAGE);

        mockMvc.perform(post("/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ausente@email.com\"}"))
                .andExpect(status().isAccepted())
                .andExpect(content().json("{\"message\":\"" + ACCEPTED_MESSAGE + "\"}"));
    }

    @Test
    void malformedEmailReturns400() throws Exception {
        when(forgotPasswordService.requestReset(eq("nao-e-email"), any())).thenThrow(new InvalidEmailException());

        mockMvc.perform(post("/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"nao-e-email\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("E-mail inválido"));
    }

    @Test
    void rateLimitReturns429() throws Exception {
        when(forgotPasswordService.requestReset(eq("voce@email.com"), any())).thenThrow(new RateLimitExceededException());

        mockMvc.perform(post("/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"voce@email.com\"}"))
                .andExpect(status().isTooManyRequests())
                .andExpect(content().string(""));
    }

    @Test
    void remainsPublicWithoutBearer() throws Exception {
        when(forgotPasswordService.requestReset(eq("voce@email.com"), any())).thenReturn(ACCEPTED_MESSAGE);

        mockMvc.perform(post("/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"voce@email.com\"}"))
                .andExpect(status().isAccepted());

        verify(forgotPasswordService).requestReset(eq("voce@email.com"), any());
        verify(authService, never()).login(any());
    }

    @Test
    void usesForwardedIpWhenPresent() throws Exception {
        when(forgotPasswordService.requestReset(eq("voce@email.com"), eq("203.0.113.10"))).thenReturn(ACCEPTED_MESSAGE);

        mockMvc.perform(post("/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Forwarded-For", "203.0.113.10, 10.0.0.1")
                        .content("{\"email\":\"voce@email.com\"}"))
                .andExpect(status().isAccepted());
    }

    @Test
    void resetPasswordReturns204WithoutToken() throws Exception {
        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"abc\",\"password\":\"novasenha\"}"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(forgotPasswordService).resetPassword(eq("abc"), eq("novasenha"), any());
    }

    @Test
    void resetPasswordInvalidLinkReturns400() throws Exception {
        org.mockito.Mockito.doThrow(new InvalidResetLinkException())
                .when(forgotPasswordService).resetPassword(eq("bad"), eq("novasenha"), any());

        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"bad\",\"password\":\"novasenha\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Link inválido ou expirado"));
    }

    @Test
    void resetPasswordWeakPasswordReturns400() throws Exception {
        org.mockito.Mockito.doThrow(new WeakPasswordException())
                .when(forgotPasswordService).resetPassword(eq("abc"), eq("short"), any());

        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"abc\",\"password\":\"short\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("A senha deve conter ao menos 8 caracteres"));
    }

    @Test
    void resetPasswordRemainsPublicWithoutBearer() throws Exception {
        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"abc\",\"password\":\"novasenha\"}"))
                .andExpect(status().isNoContent());
    }
}
