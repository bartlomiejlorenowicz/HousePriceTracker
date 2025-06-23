package com.scrapper.service;

import com.scrapper.dto.Currency;
import com.scrapper.dto.ScrapedApartment;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ApartmentRunnerService {

    private final WebDriver webDriver;

    public ApartmentRunnerService(WebDriver webDriver) {
        this.webDriver = webDriver;
    }

    public List<ScrapedApartment> scrape() {
        log.info("Start scraping from otodom");
        String baseUrl = "https://www.otodom.pl/pl/wyniki/sprzedaz/mieszkanie/podkarpackie/rzeszow/rzeszow/rzeszow?priceMin=300000&priceMax=500000&viewType=listing";
        webDriver.get(baseUrl);

        int lastPage = getPagination(webDriver);
        log.info("number of pages: {}", lastPage);

        List<ScrapedApartment> result = new ArrayList<>();

        for (int page = 1; page <= lastPage; page++) {
            String link = "https://www.otodom.pl/pl/wyniki/sprzedaz/mieszkanie/podkarpackie/rzeszow/rzeszow/rzeszow?priceMin=300000&priceMax=500000&viewType=listing&page=" + page;
            webDriver.get(link);

            List<WebElement> cards = webDriver.findElements(By.cssSelector(".e1wc8ahl0"));
            log.info("founded {} cards on page", cards.size());

            for (WebElement card : cards) {
                BigDecimal price = getPrice(card);
                if (price == null) {
                    log.info("Offer omitted - no price!");
                    continue;
                }
                String address = card.findElement(By.cssSelector(".eejmx80")).getText().trim();
                String url = card.findElement(By.cssSelector(".e17g0c820")).getAttribute("href");
                Currency currency = detectCurrency(card.findElement(By.cssSelector(".e1wc8ahl7")).getText());
                log.info("Retrieving offer: address={}, price={}, url={}, currency={}", address, price, url, currency);

                result.add(new ScrapedApartment(url, price, address, currency));
            }
        }
        return result;
    }

    private BigDecimal getPrice(WebElement card) {
        try {
            String priceText = card.findElement(By.cssSelector(".evk7nst0")).getText();
            if (priceText == null)
                return null;
            priceText = priceText.replaceAll("[^0-9]", "");
            if (priceText.isEmpty())
                return null;
            return new BigDecimal(priceText);
        } catch (Exception e) {
            log.warn("Error by price retrieving: {}", e.getMessage());
            return null;
        }
    }

    private Currency detectCurrency(String priceText) {
        if (priceText.contains("EUR")) return Currency.EUR;
        if (priceText.contains("USD")) return Currency.USD;
        return Currency.PLN;
    }

    private int getPagination(WebDriver webDriver) {
        List<WebElement> pages = webDriver.findElements(By.cssSelector("li.css-43nhzf"));

        int maxPage = 1;
        for (WebElement page : pages) {
            String text = page.getText().trim();
            try {
                int currentPage = Integer.parseInt(text);
                if (currentPage > maxPage)
                    maxPage = currentPage;
            } catch (NumberFormatException e) {
                log.info("Number format exception: {}", e.getMessage());
            }
        }
        return maxPage;
    }

}
