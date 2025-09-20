package com.scrapper.repository;

import com.scrapper.entity.ApartmentPriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApartmentPriceHistoryRepository extends JpaRepository<ApartmentPriceHistory, Long> {

    Optional<ApartmentPriceHistory> findTopByApartment_IdOrderByCollectedAtDesc(Long apartmentId);
}
