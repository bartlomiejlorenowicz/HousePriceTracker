package com.housetracker.authservice.validator.Userlogin;

import com.housetracker.authservice.entity.User;
import com.housetracker.authservice.validator.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
@Component
@Slf4j
public class UserStatusEmail extends Validator<User> {

    @Override
    public void validate(User user) {
        if (!user.isEmailVerified()){
            log.warn("Login blocked: email not verified for user={}", user.getEmail());
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "User email has not been confirmed"
            );
        }
        validateNext(user);
     }
}
