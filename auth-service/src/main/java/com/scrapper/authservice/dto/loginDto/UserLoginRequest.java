package com.scrapper.authservice.dto.loginDto;

import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserLoginRequest {

    @Enumerated
    @NotBlank
    private String email;

    @NotBlank
    private String password;
}
