package com.scrapper.controller;

import com.scrapper.dto.CreateWatchRequest;
import com.scrapper.dto.WatchDto;
import com.scrapper.entity.UserApartmentWatch;
import com.scrapper.service.WatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/watches")
@RequiredArgsConstructor
public class WatchController {

    private final WatchService watchService;

    @PostMapping
    public WatchDto create(@RequestHeader(value="X-User-Id") Long userId,
                           @RequestBody CreateWatchRequest req) {

        UserApartmentWatch w = watchService.addOrUpdateWatch(userId, req.url(), req.minPrice());
        return new WatchDto(w.getId(), w.getApartment().getId(), w.getUserId(), w.getMinPrice(), w.isActive());
    }

    @GetMapping
    public List<WatchDto> my(@RequestHeader(value="X-User-Id") Long userId,
                             @RequestParam(defaultValue = "true") boolean active) {
        return watchService.list(userId, active).stream()
                .map(w -> new WatchDto(w.getId(), w.getApartment().getId(), w.getUserId(), w.getMinPrice(), w.isActive()))
                .toList();
    }

    @DeleteMapping("/{apartmentId}")
    public void remove(@RequestHeader(value="X-User-Id") Long userId,
                       @PathVariable Long apartmentId) {
        watchService.remove(userId, apartmentId);
    }
}
