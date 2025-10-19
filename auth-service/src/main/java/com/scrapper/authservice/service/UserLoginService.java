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

        checkIfAccountIsLocked(user, email, now);
        authenticateUser(userLoginRequest, email, now, user);

        user = reloadUserIfNecessary(user, email);
        userLoginValidator.validate(user);

        resetLoginState(user, now);

        String token = jwtUtils.generateToken(user);
        log.info("user logged in: {}", email);

        return buildLoginResponse(user, token);
    }

    private void checkIfAccountIsLocked(User user, String email, LocalDateTime now) {
        if (user != null && user.getLockedUntil() != null && user.getLockedUntil().isAfter(now)) {
            log.warn("Login blocked: account locked until {} for user={}", user.getLockedUntil(), email);
            throw new UserValidationException(UserErrorType.ACCOUNT_LOCKED, "Account is locked until: " + user.getLockedUntil());
        }
    }

    private void authenticateUser(UserLoginRequest request, String email, LocalDateTime now, User user) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.getPassword())
            );
        } catch (DisabledException ex) {
            log.warn("Login attempt on disabled account: {}", email);
            throw ex;
        } catch (AuthenticationException ex) {
            log.warn("Failed login for email={}: {}", email, ex.getMessage());
            handleFailedLogin(user, now, email);
            throw new BadCredentialsException("Bad credentials");
        }
    }

    private void handleFailedLogin(User user, LocalDateTime now, String email) {
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
    }

    private User reloadUserIfNecessary(User user, String email) {
        if (user == null) {
            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new BadCredentialsException("Bad credentials"));
        }
        return user;
    }

    private void resetLoginState(User user, LocalDateTime now) {
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        user.setLastLoginAt(now);
        userRepository.save(user);
    }

    private UserAuthResponse buildLoginResponse(User user, String token) {
        return UserAuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .email(user.getEmail())
                .operationType(OperationType.LOGIN)
                .build();
    }

}
