package com.scrapper.authservice.validator.Userlogin;

import com.scrapper.authservice.entity.User;
import com.scrapper.authservice.user.UserStatus;
import com.scrapper.authservice.validator.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
@Slf4j
public class UserStatusInactive extends Validator<User> {

    @Override
    public void validate(User user) {
        if (user.getStatus() != UserStatus.ACTIVE) {
            log.warn("Login blocked: account inactive for user={}", user.getEmail());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User account is inactive.");
        }
        validateNext(user);
    }

}
