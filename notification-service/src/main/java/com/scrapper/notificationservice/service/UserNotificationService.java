package com.scrapper.notificationservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.stereotype.Service;
@Slf4j
@Service
public class UserNotificationService {
    private final Queue userQueue;
    private final TopicExchange exchange;
    private final Binding binding;
    private final EmailService emailService;

    public UserNotificationService(Queue userQueue, TopicExchange exchange, Binding binding, EmailService emailService) {
        this.userQueue = userQueue;
        this.exchange = exchange;
        this.binding = binding;
        this.emailService = emailService;
    }

    // Metoda do wysyłania powiadomienia (e-maila)
    public void sendWelcomeEmail(String userEmail, String firstName) {
        emailService.sendWelcomeEmail(userEmail, firstName);
    }


    public void sendNotification() {
        log.info("Sending notification to queue: {}", userQueue.getName());
    }
}
