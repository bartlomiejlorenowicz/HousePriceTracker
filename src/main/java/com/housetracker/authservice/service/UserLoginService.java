package com.housetracker.authservice.service;

import com.housetracker.authservice.dto.authResponse.UserAuthResponse;
import com.housetracker.authservice.dto.loginDto.UserLoginRequest;
import com.housetracker.authservice.dto.response.OperationType;
import com.housetracker.authservice.entity.User;
import com.housetracker.authservice.repository.UserRepository;
import com.housetracker.authservice.security.JwtUtils;
import com.housetracker.authservice.validator.Userlogin.UserLoginValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserLoginService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final UserLoginValidator userLoginValidator;

    public UserAuthResponse login(UserLoginRequest userLoginRequest) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    userLoginRequest.getEmail(), userLoginRequest.getPassword()
            ));
        } catch (AuthenticationException ex) {
            log.warn("Nieudane logowanie dla email={}: {}", userLoginRequest.getEmail(), ex.getMessage());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Bad credentials");
        }

        User user = userRepository.findByEmail(userLoginRequest.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Bad credentials"));

        userLoginValidator.validate(user);

        user.setFailedLoginAttempts(0);
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        String token = jwtUtils.generateToken(userLoginRequest.getEmail());
        log.info("Użytkownik zalogowany: {}", userLoginRequest.getEmail());

        return UserAuthResponse.builder()
                .token(token)
                .operationType(OperationType.LOGIN)
                .build();
    }
}
