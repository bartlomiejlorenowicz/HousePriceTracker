package com.scrapper.notificationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PriceDropEvent {
    private final Long apartmentId;
    private final String url;
    private final String oldPrice;
    private final String newPrice;
    private final Long userId;
    private final String email;
}
