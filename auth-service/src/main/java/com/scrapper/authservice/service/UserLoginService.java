package com.scrapper.authservice.service;

import com.scrapper.authservice.dto.authRegistrationResponse.UserAuthResponse;
import com.scrapper.authservice.dto.loginDto.UserLoginRequest;
import com.scrapper.authservice.dto.response.OperationType;
import com.scrapper.authservice.entity.User;
import com.scrapper.authservice.exception.UserErrorType;
import com.scrapper.authservice.exception.UserValidationException;
import com.scrapper.authservice.repository.UserRepository;
import com.scrapper.authservice.security.JwtUtils;
import com.scrapper.authservice.validator.Userlogin.UserLoginValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserLoginService {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(15);

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final UserLoginValidator userLoginValidator;
    private final Clock clock;

    @Transactional
    public UserAuthResponse login(UserLoginRequest userLoginRequest) {
        final String email = userLoginRequest.getEmail();
        final LocalDateTime now = LocalDateTime.now(clock);

        User user = userRepository.findByEmail(email).orElse(null);

        if (user != null && user.getLockedUntil() != null && user.getLockedUntil().isAfter(now)) {
            log.warn("Login blocked: account locked until {} for user={}", user.getLockedUntil(), email);
            throw new UserValidationException(UserErrorType.ACCOUNT_LOCKED, "Account is locked until: " + user.getLockedUntil());
        }

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    email, userLoginRequest.getPassword()));
        } catch (DisabledException ex) {
            log.warn("Login attempt on disabled account: {}", email);
            throw ex;
        } catch (AuthenticationException ex) {
            log.warn("Failed login for email={}: {}", email, ex.getMessage());

            if (user != null) {
                int failedAttempts = user.getFailedLoginAttempts() + 1;
                user.setFailedLoginAttempts(failedAttempts);

                if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
                    LocalDateTime lockedUntil = now.plus(LOCK_DURATION);
                    user.setLockedUntil(lockedUntil);
                    log.warn("Login blocked: account locked until {} for user={}", lockedUntil, email);
                }
                userRepository.save(user);
            }
            throw new BadCredentialsException("Bad credentials");
        }

        user = (user != null) ? user : userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Bad credentials"));

        userLoginValidator.validate(user);

        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        user.setLastLoginAt(now);
        userRepository.save(user);

        String token = jwtUtils.generateToken(email);
        log.info("user logged in: {}", email);

        return UserAuthResponse.builder()
                .token(token)
                .operationType(OperationType.LOGIN)
                .build();
    }
}
