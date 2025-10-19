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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserLoginServiceTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtUtils jwtUtils;
    @Mock private UserRepository userRepository;
    @Mock
    private UserLoginValidator userLoginValidator;
    @Mock private Clock clock;

    @InjectMocks
    private UserLoginService userLoginService;

    private final LocalDateTime fixedNow = LocalDateTime.of(2025, 10, 19, 12, 0);

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(fixedNow.atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        when(clock.instant()).thenReturn(fixedClock.instant());
        when(clock.getZone()).thenReturn(fixedClock.getZone());
    }

    private UserLoginRequest userRequest() {
        UserLoginRequest request = new UserLoginRequest();
        request.setEmail("user@example.com");
        request.setPassword("password123");
        return request;
    }

    @Test
    void shouldLoginSuccessfully() {
        UserLoginRequest request = userRequest();
        User user = new User();
        user.setEmail(request.getEmail());
        user.setFailedLoginAttempts(2);
        user.setLockedUntil(LocalDateTime.of(2025, 10, 1, 0, 0));

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(jwtUtils.generateToken(user)).thenReturn("mock-jwt");

        UserAuthResponse response = userLoginService.login(request);

        verify(authenticationManager).authenticate(any());
        verify(userLoginValidator).validate(user);

        assertEquals("mock-jwt", response.getToken());
        assertEquals(OperationType.LOGIN, response.getOperationType());
        assertEquals(0, user.getFailedLoginAttempts());
        assertNull(user.getLockedUntil());
        assertEquals(fixedNow, user.getLastLoginAt());

        verify(userRepository, times(1)).save(user);
    }

    @Test
    void shouldThrowWhenAccountIsLocked() {
        UserLoginRequest request = userRequest();
        User user = new User();
        user.setEmail(request.getEmail());
        user.setLockedUntil(fixedNow.plusMinutes(10));

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));

        UserValidationException ex = assertThrows(UserValidationException.class, () -> userLoginService.login(request));
        assertEquals(UserErrorType.ACCOUNT_LOCKED, ex.getUserErrorType());
        assertTrue(ex.getMessage().contains("Account is locked"));
    }

    @Test
    void shouldIncrementFailedAttemptsOnBadCredentials() {
        UserLoginRequest request = userRequest();
        User user = new User();
        user.setEmail(request.getEmail());
        user.setFailedLoginAttempts(2);

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        doThrow(new BadCredentialsException("Bad creds")).when(authenticationManager).authenticate(any());

        assertThrows(BadCredentialsException.class, () -> userLoginService.login(request));

        assertEquals(3, user.getFailedLoginAttempts());
        verify(userRepository).save(user);
    }

    @Test
    void shouldLockAccountAfterMaxFailedAttempts() {
        UserLoginRequest request = userRequest();
        User user = new User();
        user.setEmail(request.getEmail());
        user.setFailedLoginAttempts(4);

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        doThrow(new BadCredentialsException("Bad creds")).when(authenticationManager).authenticate(any());

        assertThrows(BadCredentialsException.class, () -> userLoginService.login(request));

        assertEquals(5, user.getFailedLoginAttempts());
        assertEquals(fixedNow.plusMinutes(15), user.getLockedUntil());
        verify(userRepository).save(user);
    }

    @Test
    void shouldThrowWhenAccountIsDisabled() {
        UserLoginRequest request = userRequest();
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(new User()));
        doThrow(new DisabledException("Account disabled")).when(authenticationManager).authenticate(any());

        assertThrows(DisabledException.class, () -> userLoginService.login(request));
    }


}