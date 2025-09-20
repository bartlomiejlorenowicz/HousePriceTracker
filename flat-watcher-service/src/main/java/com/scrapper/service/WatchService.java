package com.scrapper.service;

import com.scrapper.entity.Apartment;
import com.scrapper.entity.UserApartmentWatch;
import com.scrapper.repository.ApartmentRepository;
import com.scrapper.repository.UserApartmentWatchRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WatchService {

    private final ApartmentRepository apartmentRepo;
    private final UserApartmentWatchRepository watchRepo;

    @Transactional
    public UserApartmentWatch addOrUpdateWatch(Long userId, String urlNormalized, Integer minPrice) {
        Apartment apt = apartmentRepo.findByUrl(urlNormalized)
                .orElseGet(() -> apartmentRepo.save(
                        Apartment.builder().url(urlNormalized).build()));

        return watchRepo.findByUserIdAndApartment_Id(userId, apt.getId())
                .map(existing -> {
                    existing.setActive(true);
                    existing.setMinPrice(minPrice);
                    return existing;
                })
                .orElseGet(() -> watchRepo.save(UserApartmentWatch.builder()
                        .userId(userId)
                        .apartment(apt)
                        .minPrice(minPrice)
                        .active(true)
                        .build()));
    }

    @Transactional
    public List<UserApartmentWatch> list(Long userId, boolean onlyActive) {
        return onlyActive ? watchRepo.findAllByUserIdAndActive(userId, true)
                : watchRepo.findAllByUserIdAndActive(userId, false);
    }

    @Transactional
    public void remove(Long userId, Long apartmentId) {
        watchRepo.findByUserIdAndApartment_Id(userId, apartmentId)
                .ifPresent(w -> w.setActive(false));
    }
}
