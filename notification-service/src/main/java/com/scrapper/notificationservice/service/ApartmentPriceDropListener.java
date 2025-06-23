package com.scrapper.notificationservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scrapper.notificationservice.config.NotificationConfig;
import com.scrapper.notificationservice.dto.PriceDropEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ApartmentPriceDropListener {

    private final EmailService emailService;

    private final ObjectMapper objectMapper;

    public ApartmentPriceDropListener(EmailService emailService, ObjectMapper objectMapper) {
        this.emailService = emailService;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = NotificationConfig.APARTMENT_QUEUE)
    public void onPriceDrop(String eventJson) throws JsonProcessingException {
        PriceDropEvent event = objectMapper.readValue(eventJson, PriceDropEvent.class);
        log.info("Received price drop event for apartment: {}. Old: {}, New: {}",
                event.getUrl(), event.getOldPrice(), event.getNewPrice());

        String subject = "Price reduction for the apartment you are looking at!";
        String body = String.format(
                "Hello!\n\n" +
                        "The price of the apartment you are looking at has been reduced!\n\n" +
                        "Previous price: %s\n" +
                        "New price: %s\n\n" +
                        "See the offer: %s\n\n" +
                event.getOldPrice(),
                event.getNewPrice(),
                event.getUrl()
        );

        String userEmail = event.getEmail();
        emailService.sendPriceDropAlert(userEmail, subject, body);
    }
}
