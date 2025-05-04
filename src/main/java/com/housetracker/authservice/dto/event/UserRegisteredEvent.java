package com.housetracker.authservice.dto.event;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class UserRegisteredEvent {

    private Long userId;
    private String email;
    private String firstName;
    private String lastName;
}
