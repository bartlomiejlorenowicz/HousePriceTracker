package com.scrapper.notificationservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NotificationConfig {
    public static final String USER_EXCHANGE = "user.events.exchange";
    public static final String USER_ROUTING_KEY = "user.registered";
    public static final String USER_QUEUE = "user.registered.queue";

    @Bean
    public Queue userQueue() {
        return new Queue(USER_QUEUE);
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(USER_EXCHANGE);
    }

    @Bean
    public Binding binding() {
        return BindingBuilder.bind(userQueue()).to(exchange()).with(USER_ROUTING_KEY);
    }

    public static final String APARTMENT_EXCHANGE = "apartment.events.exchange";
    public static final String APARTMENT_ROUTING_KEY = "apartment.price-dropped";
    public static final String APARTMENT_QUEUE = "apartment.price-dropped.queue";

    @Bean
    public Queue apartmentQueue() {
        return new Queue(APARTMENT_QUEUE);
    }

    @Bean
    public TopicExchange apartmentExchange() {
        return new TopicExchange(APARTMENT_EXCHANGE);
    }

    @Bean
    public Binding apartmentBinding() {
        return BindingBuilder.bind(apartmentQueue()).to(apartmentExchange()).with(APARTMENT_ROUTING_KEY);
    }
}
