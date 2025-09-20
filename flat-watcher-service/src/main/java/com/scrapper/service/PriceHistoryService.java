package com.scrapper.service;

import com.scrapper.entity.Apartment;
import com.scrapper.entity.ApartmentPriceHistory;
import com.scrapper.entity.UserApartmentWatch;
import com.scrapper.repository.ApartmentPriceHistoryRepository;
import com.scrapper.repository.UserApartmentWatchRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PriceHistoryService {

    private final ApartmentPriceHistoryRepository historyRepo;
    private final UserApartmentWatchRepository watchRepo;

    private static final Duration NOTIFY_DEBOUNCE = Duration.ofHours(6);

    @Transactional
    public void recordSampleAndMaybeNotify(Apartment apt, int newPrice) {
        var last = historyRepo.findTopByApartment_IdOrderByCollectedAtDesc(apt.getId())
                .orElse(null);

        if (last == null || !last.getPrice().equals(newPrice)) {
            historyRepo.save(ApartmentPriceHistory.builder()
                    .apartment(apt).price(newPrice).build());
        }

        if (last != null && newPrice < last.getPrice()) {
            List<UserApartmentWatch> watchers = watchRepo.findAllByApartment_IdAndActiveTrue(apt.getId());
            LocalDateTime now = LocalDateTime.now();

            for (UserApartmentWatch w : watchers) {
                boolean minOk = (w.getMinPrice() == null) || (newPrice <= w.getMinPrice());
                boolean debounceOk = (w.getLastNotifiedAt() == null) ||
                        Duration.between(w.getLastNotifiedAt(), now).compareTo(NOTIFY_DEBOUNCE) >= 0;

                if (minOk && debounceOk) {
                    //todo: send notification to user
                    log.info("PRICE DROP user={} apt={} {} -> {}", w.getUserId(), apt.getId(),
                            last.getPrice(), newPrice);
                    w.setLastNotifiedAt(now);
                }
            }
        }
    }
}
