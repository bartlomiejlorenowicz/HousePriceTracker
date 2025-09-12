package com.scrapper.service;

import com.scrapper.dto.Currency;
import com.scrapper.dto.ScrapedApartment;
import com.scrapper.entity.Apartment;
import com.scrapper.repository.ApartmentRepository;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Service
public class ApartmentRunnerService {

    private static final String BASE_DOMAIN = "https://www.otodom.pl";
    private static final Duration WAIT_TIMEOUT = Duration.ofSeconds(10);

    private final WebDriver webDriver;
    private final ApartmentRepository apartmentRepository;

    public ApartmentRunnerService(WebDriver webDriver, ApartmentRepository apartmentRepository) {
        this.webDriver = webDriver;
        this.apartmentRepository = apartmentRepository;
    }

    public List<ScrapedApartment> scrape() {
        log.info("Start scraping from otodom");

        String baseUrl = BASE_DOMAIN + "/pl/wyniki/sprzedaz/mieszkanie/podkarpackie/rzeszow/rzeszow/rzeszow"
                + "?priceMin=300000&priceMax=500000&viewType=listing";

        webDriver.get(baseUrl);
        acceptCookiesIfVisible();
        waitForListing();

        int lastPage = getPagination(webDriver);
        log.info("number of pages: {}", lastPage);

        List<ScrapedApartment> result = new ArrayList<>();

        for (int page = 1; page <= lastPage; page++) {
            String link = baseUrl + "&page=" + page;
            webDriver.get(link);
            acceptCookiesIfVisible();
            waitForListing();

            List<WebElement> cards = new WebDriverWait(webDriver, WAIT_TIMEOUT)
                    .until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                            By.cssSelector("article[data-sentry-component='AdvertCard']")));

            log.info("found {} cards on page {}", cards.size(), page);

            for (WebElement card : cards) {
                BigDecimal price = extractPrice(card);
                if (price == null) {
                    log.info("Offer omitted - no price!");
                    continue;
                }

                WebElement linkEl = card.findElement(By.cssSelector("a[data-cy='listing-item-link']"));
                String url = linkEl.getAttribute("href");
                if (url != null && url.startsWith("/")) {
                    url = BASE_DOMAIN + url;
                }

                String address = "";
                try {
                    address = card.findElement(By.cssSelector("[data-sentry-component='Address']"))
                            .getText().trim();
                } catch (NoSuchElementException ignored) {

                }

                Currency currency = detectCurrency(card.getText());
                log.info("Retrieving offer: address={}, price={}, url={}, currency={}",
                        address, price, url, currency);

                Integer rooms = extractRoomCount(card);

                result.add(new ScrapedApartment(url, price, address, currency, rooms));
            }
        }

        updateInactiveApartments(result);
        return result;
    }

    private void acceptCookiesIfVisible() {
        clickIfPresent(By.id("onetrust-accept-btn-handler"));

        clickIfPresent(By.xpath("//button[contains(translate(., 'ACEJPTU', 'acejptu'),'akceptuj')]"));
    }

    private void waitForListing() {
        WebDriverWait wait = new WebDriverWait(webDriver, WAIT_TIMEOUT);
        try {
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.presenceOfElementLocated(
                            By.cssSelector("article[data-sentry-component='AdvertCard']")),
                    ExpectedConditions.presenceOfElementLocated(
                            By.cssSelector("a[data-cy='listing-item-link']"))
            ));
        } catch (TimeoutException e) {
            log.warn("Listing did not appear within {}s", WAIT_TIMEOUT.getSeconds());
        }
    }

    private void clickIfPresent(By by) {
        try {
            WebElement el = new WebDriverWait(webDriver, Duration.ofSeconds(2))
                    .until(ExpectedConditions.elementToBeClickable(by));
            el.click();
        } catch (Exception ignored) {
        }
    }

    private BigDecimal extractPrice(WebElement card) {
        try {
            WebElement priceEl = card.findElement(
                    By.cssSelector("[data-sentry-element='MainPrice']")
            );
            String priceText = priceEl.getText();
            String digits = priceText.replaceAll("[^0-9]", "");
            if (digits.isEmpty()) return null;
            return new BigDecimal(digits);
        } catch (Exception e) {
            log.warn("Error retrieving price: {}", e.getMessage());
            return null;
        }
    }

    private Currency detectCurrency(String text) {
        if (text == null) return Currency.PLN;
        String t = text.toUpperCase();
        if (t.contains("EUR") || t.contains("€")) return Currency.EUR;
        if (t.contains("USD") || t.contains("$")) return Currency.USD;
        return Currency.PLN;
    }

    private int getPagination(WebDriver webDriver) {
        try {
            WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofSeconds(5));
            WebElement ul = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("ul[data-cy='nexus-pagination-component']")));
            int max = 1;
            for (WebElement li : ul.findElements(By.cssSelector("li"))) {
                String aria = li.getAttribute("aria-label");
                if (aria != null && !aria.isBlank()) {
                    continue;
                }
                String num = li.getText().replaceAll("\\D+", "");
                if (!num.isEmpty()) {
                    int n = Integer.parseInt(num);
                    if (n > max) max = n;
                }
            }
            return max;
        } catch (Exception e) {
            return 1;
        }
    }

    public void updateInactiveApartments(List<ScrapedApartment> scrapedApartments) {
        List<String> scrapedUrls = scrapedApartments.stream()
                .map(ScrapedApartment::getUrl).toList();

        List<Apartment> allFromDb = apartmentRepository.findAll();

        int reactivatedCount = 0;
        int deactivatedCount = 0;

        for (Apartment apartment : allFromDb) {
            boolean shouldBeActive = scrapedUrls.contains(apartment.getUrl());
            if (apartment.getIsActive() != shouldBeActive) {
                apartment.setIsActive(shouldBeActive);
                apartmentRepository.save(apartment);
                if (!shouldBeActive) {
                    deactivatedCount++;
                    log.info("deactivated offer: {}", apartment.getUrl());
                } else {
                    reactivatedCount++;
                    log.info("reactivated offer: {}", apartment.getUrl());
                }
            }
        }

        log.info("Number of deactivated offers: {}", deactivatedCount);
        log.info("Number of activated offers: {}", reactivatedCount);
    }

    private Integer extractRoomCount(WebElement card) {
        try {
            WebElement dd = card.findElement(By.xpath(
                    ".//dt[contains(normalize-space(.), 'Liczba pokoi')]/following-sibling::dd[1]"
            ));
            String txt = dd.getText();
            String digits = txt.replaceAll("\\D+", "");
            if (!digits.isEmpty()) return Integer.parseInt(digits);

            if (txt.toLowerCase().contains("kawaler")) return 1;

            return null;
        } catch (Exception e) {
            return null;
        }
    }

}