package com.housetracker.authservice.service;

import com.scrapper.authservice.config.RabbitConfig;
import com.scrapper.authservice.dto.event.UserRegisteredEvent;
import com.scrapper.authservice.dto.registerDto.UserDto;
import com.scrapper.authservice.entity.User;
import com.scrapper.authservice.mapper.UserMapper;
import com.scrapper.authservice.repository.UserRepository;
import com.scrapper.authservice.validator.Userregistration.UserValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserRegistrationService {

    private final UserRepository userRepository;
    private final UserValidator userValidator;
    private final UserMapper userMapper;
    private final AmqpTemplate rabbit;
    private final String exchange = RabbitConfig.USER_EXCHANGE;
    private final String routingKey = RabbitConfig.REGISTER_ROUTING;

    public void register(UserDto userDto) {
        userValidator.validate(userDto);
        log.info("Starting saving user with email: {}", userDto.getEmail());

        User user = userMapper.toEntity(userDto);
        userRepository.save(user);
        log.info("User saved, publishing UserRegisteredEvent for id={}", user.getId());

        UserRegisteredEvent ev = new UserRegisteredEvent(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName()
        );

        rabbit.convertAndSend(exchange, routingKey, ev, message -> {
            return message;
        });
    }
}
