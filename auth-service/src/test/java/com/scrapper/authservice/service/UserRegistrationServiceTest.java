package com.scrapper.authservice.service;

import com.scrapper.authservice.config.RabbitConfig;
import com.scrapper.authservice.dto.event.UserRegisteredEvent;
import com.scrapper.authservice.dto.registerDto.UserDto;
import com.scrapper.authservice.entity.User;
import com.scrapper.authservice.mapper.UserMapper;
import com.scrapper.authservice.repository.UserRepository;
import com.scrapper.authservice.validator.Userregistration.UserValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserRegistrationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserValidator userValidator;

    @Mock
    private UserMapper userMapper;

    @Mock
    private AmqpTemplate rabbit;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserRegistrationService userRegistrationService;

    @Test
    void shouldRegisterUserAndPublishEvent() {
        UserDto userDto = new UserDto();
        userDto.setEmail("test@example.com");
        userDto.setFirstName("John");
        userDto.setLastName("Doe");
        userDto.setPassword("Password123!");

        User user = new User();
        user.setId(1L);
        user.setEmail(userDto.getEmail());
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());

        when(userMapper.toEntity(userDto)).thenReturn(user);
        when(passwordEncoder.encode("Password123!")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userRegistrationService.register(userDto);

        verify(userValidator).validate(userDto);
        verify(userMapper).toEntity(userDto);
        verify(passwordEncoder).encode("Password123!");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assert savedUser.getPasswordHash().equals("encodedPassword");

        verify(rabbit).convertAndSend(eq(RabbitConfig.USER_EXCHANGE), eq(RabbitConfig.REGISTER_ROUTING), any(UserRegisteredEvent.class), any());
    }

    @Test
    void shouldThrowExceptionWhenSavingFails() {
        UserDto userDto = new UserDto();
        userDto.setEmail("test@wp.pl");
        userDto.setPassword("Password123!");

        User user = new User();
        user.setEmail(userDto.getEmail());

        when(userMapper.toEntity(userDto)).thenReturn(user);
        when(passwordEncoder.encode(userDto.getPassword())).thenReturn("hashed");
        doThrow(new RuntimeException("DB error")).when(userRepository).save(any());

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                userRegistrationService.register(userDto));

        assertEquals("DB error", ex.getMessage());

        verify(rabbit, never()).convertAndSend(any(), any(), any(), any());
    }
}