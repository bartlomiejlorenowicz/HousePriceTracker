package com.housetracker.authservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String USER_EXCHANGE    = "user.events.exchange";
    public static final String REGISTER_QUEUE   = "user.registered.queue";
    public static final String REGISTER_ROUTING = "user.registered";

    @Bean
    public TopicExchange userExchange() {
        return new TopicExchange(USER_EXCHANGE);
    }

    @Bean
    public Queue userRegisteredQueue() {
        return new Queue(REGISTER_QUEUE, true);
    }

    @Bean
    public Binding bindingUserRegistered(Queue userRegisteredQueue, TopicExchange userExchange) {
        return BindingBuilder
                .bind(userRegisteredQueue)
                .to(userExchange)
                .with(REGISTER_ROUTING);
    }

    // Konwerter JSON <-> obiekt Java
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

}
