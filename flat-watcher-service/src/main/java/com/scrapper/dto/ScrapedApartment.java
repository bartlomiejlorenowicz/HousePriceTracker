package com.scrapper.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@AllArgsConstructor
@Getter
public class ScrapedApartment {
    private final String url;
    private final BigDecimal price;
    private final String address;
    private final Currency currency;
    private final Integer rooms;
}
