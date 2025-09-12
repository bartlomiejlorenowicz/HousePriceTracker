package com.scrapper.notificationservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scrapper.notificationservice.config.NotificationConfig;
import com.scrapper.notificationservice.dto.UserNotificationEvent;
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

    @RabbitListener(queues = NotificationConfig.USER_QUEUE)
    public void onUserRegistered(UserNotificationEvent userNotificationEvent) throws JsonProcessingException {
        log.info("Received UserRegisteredEvent for user: {}", userNotificationEvent.getEmail());
        String subject = "Welcome to Our Service!";
        String body = String.format("Hi %s,\n\nThank you for registering with us. We hope you enjoy using our service!", userNotificationEvent.getFirstName());
        emailService.sendWelcomeEmail(userNotificationEvent.getEmail(), body);
    }
}
