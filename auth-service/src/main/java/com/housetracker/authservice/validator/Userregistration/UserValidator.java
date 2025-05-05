package com.housetracker.authservice.validator.Userregistration;

import com.housetracker.authservice.dto.registerDto.UserDto;
import com.housetracker.authservice.validator.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserValidator {
    private final Validator<UserDto> chain;

    public UserValidator(
            UserEmailValidator emailValidator,
            UserFirstNameValidator firstNameValidator,
            UserLastNameValidator lastNameValidator,
            UserPasswordValidator passwordValidator
    ) {
        this.chain = Validator.link(
                emailValidator,
                firstNameValidator,
                lastNameValidator,
                passwordValidator
        );
    }

    public void validate(UserDto userDto) {
        log.info("Starting validation for user with email: {}", userDto.getEmail());
        chain.validate(userDto);
        log.info("Validation finished for user with email: {}", userDto.getEmail());
    }
}
