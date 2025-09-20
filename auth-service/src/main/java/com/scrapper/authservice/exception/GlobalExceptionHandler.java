package com.scrapper.authservice.exception;

import com.scrapper.authservice.exception.exceptionDto.ApiExceptionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserValidationException.class)
    public ResponseEntity<ApiExceptionResponse> handleUserValidationException(UserValidationException ex) {

        log.warn("Validation error: {}", ex.getMessage());
        return respond(ex.getUserErrorType(), ex.getMessage());
    }

    @ExceptionHandler(DefaultRoleMissingException.class)
    public ResponseEntity<ApiExceptionResponse> handleDefaultRoleMissingException(DefaultRoleMissingException ex) {

        log.warn("Default role missing: {}", ex.getMessage());
        return respond(ex.getUserErrorType(), ex.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiExceptionResponse> handleBadCredentialsException(BadCredentialsException ex) {
        log.warn("Bad credentials: {}", ex.getMessage());
        return respond(HttpStatus.UNAUTHORIZED, "Bad credentials");
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiExceptionResponse> handleDisabledException(DisabledException ex) {
        log.warn("Account disabled: {}", ex.getMessage());
        return respond(HttpStatus.FORBIDDEN, "Account disabled");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiExceptionResponse> handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        return respond(HttpStatus.FORBIDDEN, "Access denied");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiExceptionResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        log.warn("Invalid request: {}", ex.getMessage());
        return respond(HttpStatus.BAD_REQUEST, "Invalid request");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiExceptionResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        log.warn("Invalid request payload: {}", ex.getMessage());
        return respond(HttpStatus.BAD_REQUEST, "Invalid request");
    }

    @ExceptionHandler(Exception.class)
    public org.springframework.http.ResponseEntity<ApiExceptionResponse> handleGeneric(Exception ex) {
        log.error("Unhandled exception: {}", ex.getMessage());
        return respond(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiExceptionResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        log.error("Parameter type mismatch: {}", ex.getMessage());
        return respond(HttpStatus.BAD_REQUEST, "Parameter type mismatch");
    }


    private HttpStatus mapStatus(UserErrorType type) {
        return switch (type) {
            case EMAIL_ALREADY_EXISTS -> HttpStatus.CONFLICT;
            case USER_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case ACCOUNT_DISABLED -> HttpStatus.FORBIDDEN;
            case ACCOUNT_LOCKED -> HttpStatus.LOCKED;
            case DEFAULT_ROLE_MISSING -> HttpStatus.INTERNAL_SERVER_ERROR;
            default -> HttpStatus.BAD_REQUEST;
        };
    }

    private ResponseEntity<ApiExceptionResponse> respond(UserErrorType type, String message) {
        HttpStatus status = mapStatus(type);
        return respond(status, type.name() + " : " + message);
    }

    private ResponseEntity<ApiExceptionResponse> respond(HttpStatus status, String message) {
        ApiExceptionResponse body = ApiExceptionResponse.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .build();
        return ResponseEntity.status(status).body(body);
    }

}
