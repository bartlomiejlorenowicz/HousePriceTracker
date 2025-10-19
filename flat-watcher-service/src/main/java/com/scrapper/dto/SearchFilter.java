package com.scrapper.dto;

import lombok.Data;

@Data
public class SearchFilter {

    private String city;
    private Integer priceMin;
    private Integer priceMax;
    private Integer rooms;
}
