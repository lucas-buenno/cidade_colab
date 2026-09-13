package tech.cidade.colab.service;

public interface PasswordResetMailer {

    void sendResetLink(String toEmail, String resetUrl);
}
