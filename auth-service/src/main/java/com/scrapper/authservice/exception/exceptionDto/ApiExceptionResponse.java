package com.scrapper.authservice.exception.exceptionDto;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApiExceptionResponse {
    private Instant timestamp;
    private int status;
    private String error;
    private String message;
}
