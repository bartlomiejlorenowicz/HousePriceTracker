package com.scrapper.authservice.mapper;

import com.scrapper.authservice.dto.registerDto.UserDto;
import com.scrapper.authservice.entity.Role;
import com.scrapper.authservice.entity.User;
import com.scrapper.authservice.repository.RoleRepository;
import com.scrapper.authservice.user.RoleType;
import com.scrapper.authservice.user.UserStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
public class UserMapper {

    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    public UserMapper(PasswordEncoder passwordEncoder, RoleRepository roleRepository) {
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

    public User toEntity(UserDto userDto) {
        log.info("Mapping UserDto to User entity for email: {}", userDto.getEmail());

        Role userRole = roleRepository.findByRole(RoleType.USER)
                .orElseThrow(() -> new RuntimeException("Default role USER not found"));

        return User.builder()
                .firstName(userDto.getFirstName())
                .lastName(userDto.getLastName())
                .email(userDto.getEmail())
                .passwordHash(passwordEncoder.encode(userDto.getPassword()))
                .status(UserStatus.ACTIVE)
                .roles(Set.of(userRole))
                .failedLoginAttempts(0)
                .emailVerified(false)
                .lastLoginAt(null)
                .lockedUntil(null)
                .build();
    }

}
