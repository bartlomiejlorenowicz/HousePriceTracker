package com.scrapper.scheduler;

import com.scrapper.dto.ScrapedApartment;
import com.scrapper.service.ApartmentRunnerService;
import com.scrapper.service.ApartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ScrappingScheduler {

    private final ApartmentService apartmentService;
    private final ApartmentRunnerService runnerService;

    @Scheduled(cron = "0 0 6 * * *", zone = "Europe/Warsaw")
    public void dailyJob() {
        List<ScrapedApartment> scraped = runnerService.scrape();
        apartmentService.scrapeAndUpdate(scraped);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void runOnce() {
        List<ScrapedApartment> scraped = runnerService.scrape();
        apartmentService.scrapeAndUpdate(scraped);
    }

}
