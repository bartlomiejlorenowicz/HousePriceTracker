package com.housetracker.authservice.controller;

import com.housetracker.authservice.dto.registerDto.UserDto;
import com.housetracker.authservice.service.UserRegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("auth/register")
@RequiredArgsConstructor
public class UserRegistrationController {

    private final UserRegistrationService userRegistrationService;

    @PostMapping
    ResponseEntity<Void> register(@Valid @RequestBody UserDto userDto) {
        userRegistrationService.register(userDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
