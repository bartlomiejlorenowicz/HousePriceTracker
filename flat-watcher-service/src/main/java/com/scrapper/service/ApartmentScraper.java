package com.scrapper.service;

import com.scrapper.dto.Currency;
import com.scrapper.dto.ScrapedApartment;
import com.scrapper.dto.SearchFilter;
import com.scrapper.utils.PageUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class ApartmentScraper {

    private static final String BASE_DOMAIN = "https://www.otodom.pl";
    private static final String BASE_URL = BASE_DOMAIN + "/pl/wyniki/sprzedaz/mieszkanie/podkarpackie/rzeszow/rzeszow/rzeszow"
            + "?priceMin=300000&priceMax=500000&viewType=listing";

    final WebDriver webDriver;
    final PageUtils pageUtils;

    public ApartmentScraper(WebDriver webDriver, PageUtils pageUtils) {
        this.webDriver = webDriver;
        this.pageUtils = pageUtils;
    }

    public List<ScrapedApartment> scrapeApartments(SearchFilter filter) {
        String searchUrl = buildSearchUrl(filter);
        webDriver.get(searchUrl);
        pageUtils.acceptCookiesIfVisible();
        pageUtils.waitForListing();

        int lastPage = getPagination();
        List<ScrapedApartment> result = new ArrayList<>();

        for (int page = 1; page <= lastPage; page++) {
            String pageUrl = searchUrl + "&page=" + page;
            webDriver.get(pageUrl);
            pageUtils.acceptCookiesIfVisible();
            pageUtils.waitForListing();

            List<WebElement> cards = getListingCards();

            for (WebElement card : cards) {
                ScrapedApartment apartment = parseCard(card);
                if (apartment != null) {
                    result.add(apartment);
                }
            }
        }

        return result;
    }

    private List<WebElement> getListingCards() {
        return new WebDriverWait(webDriver, Duration.ofSeconds(10))
                .until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                        By.cssSelector("article[data-sentry-component='AdvertCard']")));
    }

    ScrapedApartment parseCard(WebElement card) {
        BigDecimal price = extractPrice(card);
        if (price == null) {
            return null;
        }

        String url = extractUrl(card);
        String address = extractAddress(card);
        Currency currency = detectCurrency(card.getText());
        Integer rooms = extractRoomCount(card);

        return new ScrapedApartment(url, price, address, currency, rooms);
    }

    int getPagination() {
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

    BigDecimal extractPrice(WebElement card) {
        try {
            WebElement priceEl = card.findElement(By.cssSelector("[data-sentry-element='MainPrice']"));
            String priceText = priceEl.getText(); // np. "450 000 zł"
            String digits = priceText.replaceAll("[^0-9]", "");
            if (digits.isEmpty()) return null;
            return new BigDecimal(digits);
        } catch (Exception e) {
            return null;
        }
    }

    String extractUrl(WebElement card) {
        try {
            WebElement linkEl = card.findElement(By.cssSelector("a[data-cy='listing-item-link']"));
            String url = linkEl.getAttribute("href");
            if (url != null && url.startsWith("/")) {
                return BASE_DOMAIN + url;
            }
            return url;
        } catch (Exception e) {
            return null;
        }
    }

    String extractAddress(WebElement card) {
        try {
            WebElement addressEl = card.findElement(By.cssSelector("[data-sentry-component='Address']"));
            return addressEl.getText().trim();
        } catch (Exception e) {
            return "";
        }
    }

    Currency detectCurrency(String text) {
        if (text == null) return Currency.PLN;
        String upper = text.toUpperCase();
        if (upper.contains("EUR") || upper.contains("€")) return Currency.EUR;
        if (upper.contains("USD") || upper.contains("$")) return Currency.USD;
        return Currency.PLN;
    }

    Integer extractRoomCount(WebElement card) {
        try {
            WebElement dd = card.findElement(By.xpath(".//dt[contains(normalize-space(.), 'Liczba pokoi')]/following-sibling::dd[1]"));
            String text = dd.getText().toLowerCase();

            if (text.contains("kawaler")) return 1;

            String digits = text.replaceAll("\\D+", "");
            if (!digits.isEmpty()) return Integer.parseInt(digits);

            return null;
        } catch (Exception e) {
            return null;
        }
    }

    String buildSearchUrl(SearchFilter filter) {
        String city = filter.getCity() != null ? filter.getCity().toLowerCase() : "rzeszow";
        String priceMin = filter.getPriceMin() != null ? "priceMin=" + filter.getPriceMin() : "";
        String priceMax = filter.getPriceMax() != null ? "&priceMax=" + filter.getPriceMax() : "";
        String rooms = filter.getRooms() != null ? "&roomsCount=" + filter.getRooms() : "";

        return BASE_DOMAIN + "/pl/wyniki/sprzedaz/mieszkanie/podkarpackie/" + city + "/" + city + "/" + city
                + "?" + priceMin + priceMax + "&viewType=listing" + rooms;
    }


}
