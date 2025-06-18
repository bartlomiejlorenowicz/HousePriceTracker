package com.scrapper.authservice.dto.response;

public enum OperationType {
    RESET_PASSWORD,
    FORGOT_PASSWORD,
    LOGIN,
    CHANGE_PASSWORD,
    VALIDATE_RESET_TOKEN,
    SET_NEW_PASSWORD,
    LOGIN_FAILED,
    CHANGE_USER_STATUS,
    DELETE_ACCOUNT
}
