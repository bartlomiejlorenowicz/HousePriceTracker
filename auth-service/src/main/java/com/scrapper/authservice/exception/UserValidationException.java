package com.scrapper.authservice.exception;

import lombok.Getter;

@Getter
public class UserValidationException extends RuntimeException {

    private final UserErrorType userErrorType;

    public UserValidationException(UserErrorType userErrorType, String message) {
        super(message);
        this.userErrorType = userErrorType;
    }
}
