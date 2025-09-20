package com.scrapper.authservice.mapper;

import com.scrapper.authservice.dto.registerDto.UserDto;
import com.scrapper.authservice.entity.Role;
import com.scrapper.authservice.entity.User;
import com.scrapper.authservice.service.RoleService;
import com.scrapper.authservice.user.RoleType;
import com.scrapper.authservice.user.UserStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
public class UserMapper {

    private final RoleService roleService;

    public UserMapper(RoleService roleService) {
        this.roleService = roleService;
    }

    public User toEntity(UserDto userDto) {
        log.debug("Mapping UserDto to User entity for email: {}", userDto.getEmail());

        Role userRole = roleService.getRoleReference(RoleType.USER);

        return User.builder()
                .firstName(userDto.getFirstName())
                .lastName(userDto.getLastName())
                .email(userDto.getEmail())
                .passwordHash(userDto.getPassword())
                .status(UserStatus.PENDING_EMAIL_VERIFICATION) // todo change to ACTIVE after email verification
                .roles(new HashSet<>(Set.of(userRole)))
                .failedLoginAttempts(0)
                .emailVerified(false)
                .lastLoginAt(null)
                .lockedUntil(null)
                .build();
    }

}
