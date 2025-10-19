package com.scrapper.service;

import com.scrapper.entity.Apartment;
import com.scrapper.entity.ApartmentPriceHistory;
import com.scrapper.entity.UserApartmentWatch;
import com.scrapper.repository.ApartmentPriceHistoryRepository;
import com.scrapper.repository.UserApartmentWatchRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class PriceHistoryServiceTest {

    @Mock
    private ApartmentPriceHistoryRepository historyRepo;

    @Mock
    private UserApartmentWatchRepository watchRepo;

    @InjectMocks
    private PriceHistoryService priceHistoryService;

    private final Apartment apartment = Apartment.builder()
            .id(1L)
            .build();

    @Test
    void shouldSaveNewHistoryEntryWhenPriceChanged() {
        ApartmentPriceHistory last = ApartmentPriceHistory.builder()
                .price(500_000)
                .collectedAt(LocalDateTime.now().minusDays(1))
                .build();

        when(historyRepo.findTopByApartment_IdOrderByCollectedAtDesc(1L))
                .thenReturn(Optional.of(last));

        priceHistoryService.recordSampleAndMaybeNotify(apartment, 480_000);

        verify(historyRepo).save(argThat(h -> h.getPrice() == 480_000));
    }

    @Test
    void shouldNotSaveWhenPriceUnchanged() {
        ApartmentPriceHistory last = ApartmentPriceHistory.builder()
                .price(500_000)
                .collectedAt(LocalDateTime.now().minusHours(2))
                .build();

        when(historyRepo.findTopByApartment_IdOrderByCollectedAtDesc(1L))
                .thenReturn(Optional.of(last));

        priceHistoryService.recordSampleAndMaybeNotify(apartment, 500_000);

        verify(historyRepo, never()).save(any());
    }

    @Test
    void shouldNotifyUserWhenPriceDropsAndConditionsMet() {
        ApartmentPriceHistory last = ApartmentPriceHistory.builder()
                .price(500_000)
                .collectedAt(LocalDateTime.now().minusHours(2))
                .build();

        UserApartmentWatch watch = UserApartmentWatch.builder()
                .userId(123L)
                .apartment(apartment)
                .active(true)
                .minPrice(490_000)
                .lastNotifiedAt(LocalDateTime.now().minusHours(7))
                .build();

        when(historyRepo.findTopByApartment_IdOrderByCollectedAtDesc(1L)).thenReturn(Optional.of(last));
        when(watchRepo.findAllByApartment_IdAndActiveTrue(1L)).thenReturn(List.of(watch));

        priceHistoryService.recordSampleAndMaybeNotify(apartment, 480_000);

        assertNotNull(watch.getLastNotifiedAt());
        assertTrue(Duration.between(watch.getLastNotifiedAt(), LocalDateTime.now()).getSeconds() < 5);
    }

    @Test
    void shouldNotNotifyWhenDebounceNotElapsed() {
        ApartmentPriceHistory last = ApartmentPriceHistory.builder()
                .price(500_000)
                .collectedAt(LocalDateTime.now().minusHours(1))
                .build();

        UserApartmentWatch watch = UserApartmentWatch.builder()
                .userId(123L)
                .apartment(apartment)
                .active(true)
                .minPrice(490_000)
                .lastNotifiedAt(LocalDateTime.now().minusHours(3))
                .build();

        when(historyRepo.findTopByApartment_IdOrderByCollectedAtDesc(1L)).thenReturn(Optional.of(last));
        when(watchRepo.findAllByApartment_IdAndActiveTrue(1L)).thenReturn(List.of(watch));

        priceHistoryService.recordSampleAndMaybeNotify(apartment, 480_000);

        assertEquals(LocalDateTime.now().minusHours(3).getHour(), watch.getLastNotifiedAt().getHour());
    }

    @Test
    void shouldNotNotifyWhenMinPriceNotMet() {
        ApartmentPriceHistory last = ApartmentPriceHistory.builder()
                .price(500_000)
                .collectedAt(LocalDateTime.now().minusDays(1))
                .build();

        UserApartmentWatch watch = UserApartmentWatch.builder()
                .userId(456L)
                .apartment(apartment)
                .active(true)
                .minPrice(470_000)
                .lastNotifiedAt(null)
                .build();

        when(historyRepo.findTopByApartment_IdOrderByCollectedAtDesc(1L)).thenReturn(Optional.of(last));
        when(watchRepo.findAllByApartment_IdAndActiveTrue(1L)).thenReturn(List.of(watch));

        priceHistoryService.recordSampleAndMaybeNotify(apartment, 480_000);

        assertNull(watch.getLastNotifiedAt());
    }

}