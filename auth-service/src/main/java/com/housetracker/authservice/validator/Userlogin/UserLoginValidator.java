package com.housetracker.authservice.validator.Userlogin;

import com.housetracker.authservice.entity.User;
import com.housetracker.authservice.validator.Validator;
import org.springframework.stereotype.Component;

@Component
public class UserLoginValidator {

    private final Validator<User> chain;

    public UserLoginValidator (
            UserStatusEmail userStatusEmail,
            UserStatusInactive userStatusInactive,
            UserStatusLocked userStatusLocked
    ) {
        this.chain = Validator.link(
                userStatusEmail,
                userStatusInactive,
                userStatusLocked
        );
    }

    public void validate(User user) {
        chain.validate(user);
    }
}
