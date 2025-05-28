package com.notification.notificationservice.service;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.stereotype.Service;

@Service
public class UserNotificationService {
    private final Queue userQueue;
    private final TopicExchange exchange;
    private final Binding binding;
    private final EmailService emailService;

    // Wstrzykiwanie zależności przez konstruktor
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
        System.out.println("Sending notification to queue: " + userQueue.getName());
    }
}
