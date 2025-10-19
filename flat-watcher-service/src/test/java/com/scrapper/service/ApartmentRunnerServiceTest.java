package com.scrapper.service;

import com.scrapper.dto.Currency;
import com.scrapper.dto.ScrapedApartment;
import com.scrapper.dto.SearchFilter;
import com.scrapper.entity.Apartment;
import com.scrapper.repository.ApartmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ApartmentRunnerServiceTest {

    @Mock
    private ApartmentScraper scraper;

    @Mock
    private ApartmentRepository apartmentRepository;

    @Mock
    private SearchFilter searchFilter;

    @InjectMocks
    private ApartmentRunnerService runnerService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldReturnScrapedApartmentsAndUpdateStatuses() {
        ScrapedApartment scraped1 = new ScrapedApartment("url1", new BigDecimal("400000"), "Adres 1", Currency.PLN, 2);
        ScrapedApartment scraped2 = new ScrapedApartment("url2", new BigDecimal("450000"), "Adres 2", Currency.PLN, 3);
        when(scraper.scrapeApartments(searchFilter)).thenReturn(List.of(scraped1, scraped2));

        Apartment existing1 = new Apartment();
        existing1.setUrl("url1");
        existing1.setIsActive(false);

        Apartment existing2 = new Apartment();
        existing2.setUrl("url3");
        existing2.setIsActive(true);

        when(apartmentRepository.findAll()).thenReturn(List.of(existing1, existing2));

        List<ScrapedApartment> result = runnerService.scrape();

        assertEquals(2, result.size());

        assertTrue(existing1.getIsActive());

        assertFalse(existing2.getIsActive());

        verify(apartmentRepository, times(1)).save(existing1);
        verify(apartmentRepository, times(1)).save(existing2);
    }

    @Test
    void shouldNotSaveAnythingIfNoStatusChanges() {
        ScrapedApartment scraped = new ScrapedApartment("url1", new BigDecimal("400000"), "Adres 1", Currency.PLN, 2);
        when(scraper.scrapeApartments(searchFilter)).thenReturn(List.of(scraped));

        Apartment apartment = new Apartment();
        apartment.setUrl("url1");
        apartment.setIsActive(true);

        when(apartmentRepository.findAll()).thenReturn(List.of(apartment));

        runnerService.scrape();

        verify(apartmentRepository, never()).save(any());
    }

}