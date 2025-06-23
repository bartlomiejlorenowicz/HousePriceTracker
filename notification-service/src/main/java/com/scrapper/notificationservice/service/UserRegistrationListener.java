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

    private final ObjectMapper objectMapper;

    public UserRegistrationListener(EmailService emailService, ObjectMapper objectMapper) {
        this.emailService = emailService;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = NotificationConfig.USER_QUEUE)
    public void onUserRegistered(String event) throws JsonProcessingException {
        UserNotificationEvent userNotificationEvent = objectMapper.readValue(event, UserNotificationEvent.class);
        log.info("Received UserRegisteredEvent for user: {}", userNotificationEvent.getEmail());
        String subject = "Welcome to Our Service!";
        String body = String.format("Hi %s,\n\nThank you for registering with us. We hope you enjoy using our service!", userNotificationEvent.getFirstName());
        emailService.sendWelcomeEmail(userNotificationEvent.getEmail(), body);
    }
}
