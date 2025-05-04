package com.housetracker.authservice.validator.Userregistration;

import com.housetracker.authservice.dto.registerDto.UserDto;
import com.housetracker.authservice.exception.UserErrorType;
import com.housetracker.authservice.exception.UserValidationException;
import com.housetracker.authservice.validator.Validator;
import org.springframework.stereotype.Component;

import static java.util.Objects.isNull;

@Component
public class UserLastNameValidator extends Validator<UserDto> {

    @Override
    public void validate(UserDto userDto) {
        String lastName = userDto.getLastName();
        if (isNull(lastName)) {
            throw new UserValidationException(UserErrorType.INVALID_LAST_NAME, "lastname i null");
        }

        lastName = lastName.trim();

        if (lastName.isEmpty()) {
            throw new UserValidationException(UserErrorType.INVALID_LAST_NAME, "last name cannot be empty or whitespace only");
        }

        boolean lastNameTooShort = lastName.length() < 2;
        if (lastNameTooShort) {
            throw new UserValidationException(UserErrorType.INVALID_LAST_NAME, "last name must have at least 2 letters");
        }
        boolean lastNameTooLong = lastName.length() > 30;
        if (lastNameTooLong) {
            throw new UserValidationException(UserErrorType.INVALID_LAST_NAME, "last name must have maximum 30 letters");
        }

        String lastNameRegex = "^[A-Za-zĄąĆćĘęŁłŃńÓóŚśŹźŻż\\-']+$";
        if (!lastName.matches(lastNameRegex)) {
            throw new UserValidationException(UserErrorType.INVALID_LAST_NAME, "last name contains invalid characters");
        }

        validateNext(userDto);
    }
}
