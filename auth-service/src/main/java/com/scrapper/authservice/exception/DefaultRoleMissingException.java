package com.scrapper.authservice.exception;

import lombok.Getter;

@Getter
public class DefaultRoleMissingException extends RuntimeException {

    private final UserErrorType userErrorType;

    public DefaultRoleMissingException(UserErrorType userErrorType, String message) {
        super(message);
        this.userErrorType = userErrorType;
    }
}
