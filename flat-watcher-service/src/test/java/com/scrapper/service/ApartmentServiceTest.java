package com.scrapper.service;

import com.scrapper.config.RabbitConfig;
import com.scrapper.dto.Currency;
import com.scrapper.dto.PriceDropEvent;
import com.scrapper.dto.ScrapedApartment;
import com.scrapper.entity.Apartment;
import com.scrapper.repository.ApartmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class ApartmentServiceTest {

    @Mock
    private ApartmentRepository apartmentRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private ApartmentService apartmentService;

    @Test
    void shouldSaveNewApartmentIfNotExists() {
        ScrapedApartment scraped = new ScrapedApartment(
                "http://test-url",
                new BigDecimal("400000"),
                "Test Address",
                Currency.PLN,
                3
        );

        when(apartmentRepository.findByUrl(scraped.getUrl()))
                .thenReturn(Optional.empty());

        apartmentService.scrapeAndUpdate(List.of(scraped));

        verify(apartmentRepository, times(1)).save(argThat(apartment ->
                apartment.getUrl().equals(scraped.getUrl()) &&
                        apartment.getPrice().equals(scraped.getPrice()) &&
                        apartment.getInitialPrice().equals(scraped.getPrice()) &&
                        apartment.getAddress().equals(scraped.getAddress()) &&
                        apartment.getRoomCount() == 3 &&
                        apartment.getCurrency() == scraped.getCurrency() &&
                        apartment.getIsActive() &&
                        apartment.getAddedAt() != null &&
                        apartment.getLastCheckedAt() != null
        ));

        verifyNoInteractions(rabbitTemplate);
    }

    @Test
    void shouldUpdateApartmentWithoutPriceChange() {
        ScrapedApartment scraped = new ScrapedApartment(
                "http://existing-url",
                new BigDecimal("500000"),
                "Some Address",
                Currency.PLN,
                2
        );

        Apartment existing = Apartment.builder()
                .id(1L)
                .url("http://existing-url")
                .price(new BigDecimal("500000"))
                .initialPrice(new BigDecimal("500000"))
                .address("Some Address")
                .roomCount(2)
                .currency(Currency.PLN)
                .isActive(true)
                .addedAt(LocalDateTime.now().minusDays(5))
                .lastCheckedAt(LocalDateTime.now().minusDays(1))
                .build();

        when(apartmentRepository.findByUrl(scraped.getUrl()))
                .thenReturn(Optional.of(existing));

        apartmentService.scrapeAndUpdate(List.of(scraped));

        verify(apartmentRepository).save(existing);
        verifyNoInteractions(rabbitTemplate);
    }

    @Test
    void shouldUpdatePriceAndSendPriceDropEvent() {
        ScrapedApartment scraped = new ScrapedApartment(
                "http://existing-url",
                new BigDecimal("450000"),
                "Some Address",
                Currency.PLN,
                2
        );

        Apartment existing = Apartment.builder()
                .id(42L)
                .url("http://existing-url")
                .price(new BigDecimal("500000"))
                .initialPrice(new BigDecimal("500000"))
                .address("Some Address")
                .roomCount(2)
                .currency(Currency.PLN)
                .isActive(true)
                .addedAt(LocalDateTime.now().minusDays(10))
                .lastCheckedAt(LocalDateTime.now().minusDays(2))
                .build();

        when(apartmentRepository.findByUrl(scraped.getUrl()))
                .thenReturn(Optional.of(existing));

        apartmentService.scrapeAndUpdate(List.of(scraped));

        verify(apartmentRepository).save(existing);

        verify(rabbitTemplate).convertAndSend(
                eq(RabbitConfig.EXCHANGE),
                eq(RabbitConfig.ROUTING_KEY),
                any(PriceDropEvent.class)
        );

    }

    @Test
    void shouldHandleNullRoomCountGracefully() {
        // given
        ScrapedApartment scraped = new ScrapedApartment(
                "http://test-url",
                new BigDecimal("400000"),
                "Test Address",
                Currency.PLN,
                null
        );

        when(apartmentRepository.findByUrl(scraped.getUrl()))
                .thenReturn(Optional.empty());

        apartmentService.scrapeAndUpdate(List.of(scraped));

        verify(apartmentRepository).save(argThat(apartment ->
                apartment.getRoomCount() == 0
        ));
    }

    @Test
    void shouldDoNothingWhenListIsEmpty() {
        apartmentService.scrapeAndUpdate(List.of());

        verifyNoInteractions(apartmentRepository, rabbitTemplate);
    }

}