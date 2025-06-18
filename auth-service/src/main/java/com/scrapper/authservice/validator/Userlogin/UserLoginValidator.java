package com.scrapper.authservice.validator.Userlogin;

import com.scrapper.authservice.entity.User;
import com.scrapper.authservice.validator.Validator;
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
