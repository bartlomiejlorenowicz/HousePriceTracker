package com.scrapper.controller;

import com.scrapper.dto.PriceSampleRequest;
import com.scrapper.entity.Apartment;
import com.scrapper.repository.ApartmentRepository;
import com.scrapper.service.PriceHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dev")
@Profile("dev")
@RequiredArgsConstructor
public class PriceHistoryDevController {

    private final ApartmentRepository apartmentRepo;
    private final PriceHistoryService priceHistoryService;

    @PostMapping("/price-sample")
    public void addPrice(@RequestBody PriceSampleRequest req) {
        Apartment apt = apartmentRepo.findById(req.apartmentId())
                .orElseThrow(() -> new IllegalArgumentException("Apartment not found: " + req.apartmentId()));
        priceHistoryService.recordSampleAndMaybeNotify(apt, req.price());
    }

}
