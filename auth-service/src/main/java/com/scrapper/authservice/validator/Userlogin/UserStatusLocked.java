package com.scrapper.authservice.validator.Userlogin;

import com.scrapper.authservice.entity.User;
import com.scrapper.authservice.validator.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Component
@Slf4j
public class UserStatusLocked extends Validator<User> {

    @Override
    public void validate(User user) {
        LocalDateTime lockedUntil = user.getLockedUntil();
        if (user.getLockedUntil() != null
                && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            log.warn("Login blocked: account locked until {} for user={}", lockedUntil, user.getEmail());
            throw new ResponseStatusException(HttpStatus.LOCKED, "User account is locked to: " + user.getLockedUntil());
        }
        validateNext(user);
    }
}
