package com.scrapper.notificationservice.service;

import com.scrapper.notificationservice.validator.EmailValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendWelcomeEmail(String to, String firstName) {

        if (!EmailValidator.isValidEmail(to)) {
            log.error("Invalid email address: {}", to);
            return;
        }

        String subject = "Welcome to Our Service!";
        String body = String.format("Hi %s,\n\nThank you for registering with us. We hope you enjoy using our service!", firstName);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("lorenowiczbartlomiej@gmail.com");
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }
}
