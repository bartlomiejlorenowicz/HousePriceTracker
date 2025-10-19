package com.scrapper.utils;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class PageUtils {

    private final WebDriver driver;

    public PageUtils(WebDriver driver) {
        this.driver = driver;
    }

    public void acceptCookiesIfVisible() {
        clickIfPresent(By.id("onetrust-accept-btn-handler"));
        clickIfPresent(By.xpath("//button[contains(translate(., 'ACEJPTU', 'acejptu'),'akceptuj')]"));
    }

    public void waitForListing() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        try {
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.presenceOfElementLocated(
                            By.cssSelector("article[data-sentry-component='AdvertCard']")),
                    ExpectedConditions.presenceOfElementLocated(
                            By.cssSelector("a[data-cy='listing-item-link']"))
            ));
        } catch (TimeoutException ignored) {
        }
    }

    private void clickIfPresent(By by) {
        try {
            WebElement el = new WebDriverWait(driver, Duration.ofSeconds(2))
                    .until(ExpectedConditions.elementToBeClickable(by));
            el.click();
        } catch (Exception ignored) {
        }
    }
}
