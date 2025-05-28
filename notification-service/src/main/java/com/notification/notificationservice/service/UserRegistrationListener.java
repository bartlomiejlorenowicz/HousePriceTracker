package com.notification.notificationservice.service;

import com.housetracker.authservice.dto.event.UserRegisteredEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
@Slf4j
@Service
public class UserRegistrationListener {

    private final EmailService emailService;

    public UserRegistrationListener(EmailService emailService) {
        this.emailService = emailService;
    }

    @RabbitListener(queues = "user.registered.queue")
    public void onUserRegistered(UserRegisteredEvent event) {
        log.info("Received UserRegisteredEvent for user: {}", event.getEmail());
        String subject = "Welcome to Our Service!";
        String body = String.format("Hi %s,\n\nThank you for registering with us. We hope you enjoy using our service!", event.getFirstName());
        emailService.sendWelcomeEmail(event.getEmail(), body);
    }

}
