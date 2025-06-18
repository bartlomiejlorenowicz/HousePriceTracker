package com.scrapper.controller;

import com.scrapper.entity.Apartment;
import com.scrapper.repository.ApartmentRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@RestController
@RequestMapping("/api/apartments")
@RequiredArgsConstructor
public class ApartmentController {

    private final ApartmentRepository repository;

    @GetMapping
    public List<Apartment> getAll() {
        return repository.findAll();
    }

    @PostMapping
    public Apartment createApartment(@RequestBody Apartment apartment) {
        return repository.save(apartment);
    }
}
