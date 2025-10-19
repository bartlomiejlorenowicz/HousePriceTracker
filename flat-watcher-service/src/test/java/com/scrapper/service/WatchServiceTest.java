package com.scrapper.service;

import com.scrapper.entity.Apartment;
import com.scrapper.entity.UserApartmentWatch;
import com.scrapper.repository.ApartmentRepository;
import com.scrapper.repository.UserApartmentWatchRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WatchServiceTest {

    @Mock
    private ApartmentRepository apartmentRepo;

    @Mock
    private UserApartmentWatchRepository watchRepo;

    @InjectMocks
    private WatchService watchService;

    @Test
    void shouldCreateNewApartmentAndWatchWhenApartmentNotExists() {
        String url = "https://otodom.pl/apartment/123";
        Long userId = 1L;
        Integer minPrice = 450000;

        Apartment newApartment = Apartment.builder()
                .id(100L)
                .url(url)
                .build();

        when(apartmentRepo.findByUrl(url)).thenReturn(Optional.empty());
        when(apartmentRepo.save(any())).thenReturn(newApartment);
        when(watchRepo.findByUserIdAndApartment_Id(userId, newApartment.getId())).thenReturn(Optional.empty());

        UserApartmentWatch savedWatch = UserApartmentWatch.builder()
                .id(1L)
                .userId(userId)
                .apartment(newApartment)
                .minPrice(minPrice)
                .active(true)
                .build();

        when(watchRepo.save(any())).thenReturn(savedWatch);

        UserApartmentWatch result = watchService.addOrUpdateWatch(userId, url, minPrice);

        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(url, result.getApartment().getUrl());
        assertEquals(minPrice, result.getMinPrice());
        assertTrue(result.isActive());

        verify(apartmentRepo).save(any());
        verify(watchRepo).save(any());
    }

    @Test
    void shouldUpdateExistingWatch() {
        String url = "https://otodom.pl/apartment/456";
        Long userId = 2L;
        Integer newMinPrice = 400000;

        Apartment apartment = Apartment.builder()
                .id(200L)
                .url(url)
                .build();

        UserApartmentWatch existingWatch = UserApartmentWatch.builder()
                .id(2L)
                .userId(userId)
                .apartment(apartment)
                .minPrice(420000)
                .active(false)
                .build();

        when(apartmentRepo.findByUrl(url)).thenReturn(Optional.of(apartment));
        when(watchRepo.findByUserIdAndApartment_Id(userId, apartment.getId())).thenReturn(Optional.of(existingWatch));

        UserApartmentWatch result = watchService.addOrUpdateWatch(userId, url, newMinPrice);

        assertEquals(newMinPrice, result.getMinPrice());
        assertTrue(result.isActive());
        assertSame(existingWatch, result);

        verify(watchRepo, never()).save(any());
    }

    @Test
    void shouldReturnOnlyActiveWatches() {
        Long userId = 3L;
        UserApartmentWatch w1 = UserApartmentWatch.builder().id(1L).active(true).build();
        UserApartmentWatch w2 = UserApartmentWatch.builder().id(2L).active(true).build();

        when(watchRepo.findAllByUserIdAndActive(userId, true)).thenReturn(List.of(w1, w2));

        List<UserApartmentWatch> result = watchService.list(userId, true);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(UserApartmentWatch::isActive));
    }

    @Test
    void shouldReturnOnlyInactiveWatches() {
        Long userId = 4L;
        UserApartmentWatch w1 = UserApartmentWatch.builder().id(3L).active(false).build();

        when(watchRepo.findAllByUserIdAndActive(userId, false)).thenReturn(List.of(w1));

        List<UserApartmentWatch> result = watchService.list(userId, false);

        assertEquals(1, result.size());
        assertFalse(result.get(0).isActive());
    }

    @Test
    void shouldDeactivateExistingWatch() {
        Long userId = 5L;
        Long apartmentId = 99L;

        UserApartmentWatch existing = UserApartmentWatch.builder()
                .id(99L)
                .userId(userId)
                .active(true)
                .build();

        when(watchRepo.findByUserIdAndApartment_Id(userId, apartmentId)).thenReturn(Optional.of(existing));

        watchService.remove(userId, apartmentId);

        assertFalse(existing.isActive());
    }

    @Test
    void shouldDoNothingIfWatchNotExists() {
        Long userId = 6L;
        Long apartmentId = 77L;

        when(watchRepo.findByUserIdAndApartment_Id(userId, apartmentId)).thenReturn(Optional.empty());

        watchService.remove(userId, apartmentId);

        verify(watchRepo, never()).save(any());
    }

}