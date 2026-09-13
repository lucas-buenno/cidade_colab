package tech.cidade.colab.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class SmtpPasswordResetMailer implements PasswordResetMailer {

    private static final String HTML_TEMPLATE = loadHtmlTemplate();

    private final JavaMailSender mailSender;
    private final String from;

    public SmtpPasswordResetMailer(
            JavaMailSender mailSender,
            @Value("${password-reset.from}") String from
    ) {
        this.mailSender = mailSender;
        this.from = from;
    }

    @Override
    public void sendResetLink(String toEmail, String resetUrl) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setFrom(from);
            helper.setTo(toEmail);
            helper.setSubject("Redefinir senha — Cidade Colab");
            helper.setText(plainBody(resetUrl), htmlBody(resetUrl));
            mailSender.send(message);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to send password reset email", e);
        }
    }

    static String htmlBody(String resetUrl) {
        String safeUrl = escapeHtml(resetUrl);
        return HTML_TEMPLATE.replace("${link}", safeUrl);
    }

    private static String plainBody(String resetUrl) {
        return """
                Recebemos um pedido para atualizar a senha da sua conta no Cidade Colab.

                Abra o link abaixo (expira em 15 minutos):
                %s

                Se você não pediu isso, ignore este e-mail. Nada será alterado.
                """.formatted(resetUrl);
    }

    private static String escapeHtml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("\"", "&quot;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private static String loadHtmlTemplate() {
        try {
            return new ClassPathResource("mail/password-reset.html")
                    .getContentAsString(StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Missing mail/password-reset.html", e);
        }
    }
}
