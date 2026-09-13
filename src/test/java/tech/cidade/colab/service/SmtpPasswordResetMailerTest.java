package tech.cidade.colab.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SmtpPasswordResetMailerTest {

    @Test
    void htmlTemplateReplacesLinkPlaceholder() {
        String html = SmtpPasswordResetMailer.htmlBody("http://localhost:5173/redefinir-senha?token=abc");

        assertTrue(html.contains("cidade.colab"));
        assertTrue(html.contains("href=\"http://localhost:5173/redefinir-senha?token=abc\""));
        assertTrue(html.contains("Redefinir senha"));
        assertTrue(!html.contains("${link}"));
    }
}
