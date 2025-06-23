package com.scrapper.service;

import com.scrapper.config.RabbitConfig;
import com.scrapper.dto.Currency;
import com.scrapper.dto.PriceDropEvent;
import com.scrapper.dto.ScrapedApartment;
import com.scrapper.entity.Apartment;
import com.scrapper.repository.ApartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ApartmentService {
    private final ApartmentRepository repository;
    private final RabbitTemplate rabbitTemplate;

    public void scrapeAndUpdate(List<ScrapedApartment> scraped) {
        LocalDateTime now = LocalDateTime.now();

        for (ScrapedApartment scrapedApartment : scraped) {
            repository.findByUrl(scrapedApartment.getUrl())
                    .ifPresentOrElse(existing -> {
                        BigDecimal newPrice = scrapedApartment.getPrice();
                        BigDecimal oldPrice = existing.getPrice();
                        existing.setLastCheckedAt(now);

                        if (newPrice.compareTo(oldPrice) < 0) {
                            existing.setPrice(scrapedApartment.getPrice());
                            repository.save(existing);

                        PriceDropEvent event = new PriceDropEvent(
                                existing.getId(),
                                existing.getUrl(),
                                String.valueOf(oldPrice),
                                String.valueOf(newPrice),
                                existing.getUserId()
                        );
                            rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.ROUTING_KEY, event);
                        } else {
                            repository.save(existing);
                        }
            },() -> {
                        Apartment a = Apartment.builder()
                                .userId(0L)
                                .url(scrapedApartment.getUrl())
                                .price(scrapedApartment.getPrice())
                                .initialPrice(scrapedApartment.getPrice())
                                .address(scrapedApartment.getAddress())
                                .addedAt(now)
                                .lastCheckedAt(now)
                                .roomCount(0)
                                .currency(scrapedApartment.getCurrency())
                                .isActive(true)
                                .build();
                        repository.save(a);
                    });
        }
    }
}
