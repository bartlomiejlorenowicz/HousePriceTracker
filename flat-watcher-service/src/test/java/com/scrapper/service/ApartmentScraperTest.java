package com.scrapper.service;

import com.scrapper.dto.Currency;
import com.scrapper.dto.ScrapedApartment;
import com.scrapper.dto.SearchFilter;
import com.scrapper.utils.PageUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ApartmentScraperTest {

    private ApartmentScraper scraper;

    @Mock
    private WebDriver mockWebDriver;

    @Mock
    private PageUtils pageUtils;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        scraper = new ApartmentScraper(mockWebDriver, pageUtils);
    }

    @Test
    void shouldParseCardCorrectly() {
        WebElement card = mock(WebElement.class);
        WebElement priceEl = mock(WebElement.class);
        WebElement linkEl = mock(WebElement.class);
        WebElement addressEl = mock(WebElement.class);
        WebElement dd = mock(WebElement.class);

        when(card.findElement(By.cssSelector("[data-sentry-element='MainPrice']"))).thenReturn(priceEl);
        when(priceEl.getText()).thenReturn("450 000 zł");

        when(card.findElement(By.cssSelector("a[data-cy='listing-item-link']"))).thenReturn(linkEl);
        when(linkEl.getAttribute("href")).thenReturn("/oferta/123");

        when(card.findElement(By.cssSelector("[data-sentry-component='Address']"))).thenReturn(addressEl);
        when(addressEl.getText()).thenReturn("Rzeszów, Śródmieście");

        when(card.getText()).thenReturn("Cena: 450 000 zł");

        when(card.findElement(By.xpath(".//dt[contains(normalize-space(.), 'Liczba pokoi')]/following-sibling::dd[1]"))).thenReturn(dd);
        when(dd.getText()).thenReturn("3 pokoje");

        ScrapedApartment result = scraper.parseCard(card);

        assertNotNull(result);
        assertEquals("https://www.otodom.pl/oferta/123", result.getUrl());
        assertEquals(new BigDecimal("450000"), result.getPrice());
        assertEquals("Rzeszów, Śródmieście", result.getAddress());
        assertEquals(Currency.PLN, result.getCurrency());
        assertEquals(3, result.getRooms());
    }

    @Test
    void shouldReturnNullIfPriceIsMissing() {
        WebElement card = mock(WebElement.class);
        when(card.findElement(By.cssSelector("[data-sentry-element='MainPrice']"))).thenThrow(new RuntimeException("No price"));

        ScrapedApartment result = scraper.parseCard(card);
        assertNull(result);
    }

    @Test
    void shouldDetectCurrencyPLN() {
        Currency currency = scraper.detectCurrency("450 000 zł");
        assertEquals(Currency.PLN, currency);
    }

    @Test
    void shouldDetectCurrencyEUR() {
        Currency currency = scraper.detectCurrency("Cena: 200 000 EUR");
        assertEquals(Currency.EUR, currency);
    }

    @Test
    void shouldDetectCurrencyUSD() {
        Currency currency = scraper.detectCurrency("Cena: $150 000");
        assertEquals(Currency.USD, currency);
    }

    @Test
    void shouldReturnOneRoomForStudio() {
        WebElement card = mock(WebElement.class);
        WebElement dd = mock(WebElement.class);

        when(card.findElement(By.xpath(".//dt[contains(normalize-space(.), 'Liczba pokoi')]/following-sibling::dd[1]"))).thenReturn(dd);
        when(dd.getText()).thenReturn("kawalerka");

        Integer rooms = scraper.extractRoomCount(card);
        assertEquals(1, rooms);
    }

    @Test
    void shouldBuildUrlCorrectly() {
        SearchFilter filter = new SearchFilter();
        filter.setCity("rzeszow");
        filter.setPriceMin(300000);
        filter.setPriceMax(500000);
        filter.setRooms(2);

        String url = scraper.buildSearchUrl(filter);

        assertTrue(url.contains("rzeszow"));
        assertTrue(url.contains("priceMin=300000"));
        assertTrue(url.contains("priceMax=500000"));
        assertTrue(url.contains("roomsCount=2"));
    }


}