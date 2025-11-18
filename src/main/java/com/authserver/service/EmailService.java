package com.authserver.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendVerificationEmail(String to, String token) {
        String subject = "Email Verification";
        String verificationUrl = "http://localhost:8080/api/auth/verify-email?token=" + token;
        String message = String.format(
            "Welcome! Please verify your email address by clicking the link below:\n\n%s\n\n" +
            "This link will expire in 24 hours.\n\n" +
            "If you didn't create an account, please ignore this email.",
            verificationUrl
        );

        sendEmail(to, subject, message);
    }

    public void sendPasswordResetEmail(String to, String token) {
        String subject = "Password Reset Request";
        String resetUrl = "http://localhost:8080/api/auth/reset-password?token=" + token;
        String message = String.format(
            "You have requested to reset your password. Click the link below to proceed:\n\n%s\n\n" +
            "This link will expire in 1 hour.\n\n" +
            "If you didn't request a password reset, please ignore this email.",
            resetUrl
        );

        sendEmail(to, subject, message);
    }

    public void sendEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to: {}", to, e);
            throw new RuntimeException("Failed to send email: " + e.getMessage());
        }
    }
}
