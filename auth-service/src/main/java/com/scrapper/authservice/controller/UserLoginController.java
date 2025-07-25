package com.scrapper.authservice.controller;

import com.scrapper.authservice.dto.authResponse.UserAuthResponse;
import com.scrapper.authservice.dto.loginDto.UserLoginRequest;
import com.scrapper.authservice.service.UserLoginService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("auth/login")
@RequiredArgsConstructor
public class UserLoginController {

    private final UserLoginService userLoginService;

    @PostMapping
    public ResponseEntity<UserAuthResponse> login(@Valid @RequestBody UserLoginRequest userLoginRequest) {
            UserAuthResponse userAuthResponse = userLoginService.login(userLoginRequest);
        return ResponseEntity.ok(userAuthResponse);
    }
}
