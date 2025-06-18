package com.scrapper.authservice.dto.authResponse;

import com.scrapper.authservice.dto.response.OperationType;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class UserAuthResponse {

    private String token;

    @Builder.Default
    private Instant timestamp = Instant.now();

    private OperationType operationType;
}
