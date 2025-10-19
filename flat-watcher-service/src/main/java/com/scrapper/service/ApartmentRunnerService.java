package com.scrapper.service;

import com.scrapper.dto.ScrapedApartment;
import com.scrapper.dto.SearchFilter;
import com.scrapper.entity.Apartment;
import com.scrapper.repository.ApartmentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class ApartmentRunnerService {

    private final ApartmentScraper scraper;
    private final ApartmentRepository apartmentRepository;
    private final SearchFilter searchFilter;

    public ApartmentRunnerService(ApartmentScraper scraper, ApartmentRepository apartmentRepository, SearchFilter searchFilter) {
        this.scraper = scraper;
        this.apartmentRepository = apartmentRepository;
        this.searchFilter = searchFilter;
    }

    public List<ScrapedApartment> scrape() {
        log.info("Start scraping from otodom");

        List<ScrapedApartment> scrapedApartments = scraper.scrapeApartments(searchFilter);
        updateInactiveApartments(scrapedApartments);

        return scrapedApartments;
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

                if (shouldBeActive) {
                    reactivatedCount++;
                    log.info("Reactivated: {}", apartment.getUrl());
                } else {
                    deactivatedCount++;
                    log.info("Deactivated: {}", apartment.getUrl());
                }
            }
        }

        log.info("Deactivated: {}, Reactivated: {}", deactivatedCount, reactivatedCount);
    }
}