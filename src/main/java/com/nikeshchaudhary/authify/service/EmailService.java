package com.nikeshchaudhary.authify.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;

    @Value("${spring.mail.properties.mail.smtp.from}")
    private String fromEmail;

    public void sendWelcomeEmail(String name, String email) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(fromEmail);
        message.setTo(email);
        message.setSubject("Welcome to Authify!");
        message.setText("Hello " + name + ",\n\n"
                + "Thank you for registering with Authify. We're excited to have you on board!\n\n"
                + "Regards,\n"
                + "The Authify Team");

        mailSender.send(message);
    }

    public void sendVerificationOtp(String name, String email, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(fromEmail);
        message.setTo(email);
        message.setSubject("Account Verification OTP");
        message.setText("Hello " + name + ",\n\n"
                + "Thank you for registering. Please use the following One-Time Password (OTP) to verify your account: "
                + otp + "\n\n"
                + "This OTP is valid for 5 minutes. Do not share it with anyone.\n\n"
                + "Regards,\n"
                + "The Authify Team");

        mailSender.send(message);
    }

    public void sendResetOtpEmail(String name, String email, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(fromEmail);
        message.setTo(email);
        message.setSubject("Password Reset Request");
        message.setText("Hello " + name + ",\n\n"
                + "You have requested to reset your password. Your One-Time Password (OTP) is: " + otp + "\n\n"
                + "This OTP is valid for 5 minutes. Do not share it with anyone.\n\n"
                + "Regards,\n"
                + "The Authify Team");

        mailSender.send(message);
    }

}