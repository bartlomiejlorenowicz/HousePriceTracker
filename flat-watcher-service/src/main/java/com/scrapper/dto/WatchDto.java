package com.scrapper.dto;

public record WatchDto(Long id, Long apartmentId, Long userId, Integer minPrice, boolean active) {}