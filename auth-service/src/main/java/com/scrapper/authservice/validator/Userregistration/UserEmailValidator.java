package com.scrapper.authservice.validator.Userregistration;

import com.scrapper.authservice.dto.registerDto.UserDto;
import com.scrapper.authservice.exception.UserErrorType;
import com.scrapper.authservice.exception.UserValidationException;
import com.scrapper.authservice.repository.UserRepository;
import com.scrapper.authservice.validator.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static java.util.Objects.isNull;

@Slf4j
@Component
public class UserEmailValidator extends Validator<UserDto> {

    private static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";

    private final UserRepository userRepository;

    public UserEmailValidator(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    @Override
    public void validate(UserDto userDto) {
        String email = userDto.getEmail();

        if (isNull(email)) {
            throw new UserValidationException(UserErrorType.INVALID_EMAIL, "email is null");
        }
        boolean incorrectEmail = !email.matches(EMAIL_REGEX);
        if (incorrectEmail) {
            throw new UserValidationException(UserErrorType.INVALID_EMAIL, "email is not valid");
        }
        boolean emailExistInDatabase = userRepository.existsByEmail(email);
        if (emailExistInDatabase) {
            throw new UserValidationException(UserErrorType.EMAIL_ALREADY_EXISTS, "the given e-mail exists in the database");
        }
        validateNext(userDto);
    }
}
