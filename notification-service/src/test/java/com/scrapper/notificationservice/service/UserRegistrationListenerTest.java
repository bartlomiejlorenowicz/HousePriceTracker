package com.scrapper.notificationservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.scrapper.notificationservice.dto.UserNotificationEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserRegistrationListenerTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private UserRegistrationListener listener;

    @Test
    void shouldProcessUserRegisteredEventCorrectly() throws JsonProcessingException {
        UserNotificationEvent event = new UserNotificationEvent();
        event.setEmail("test123@wp.pl");
        event.setFirstName("Kamil");
        event.setLastName("Kowalski");
        event.setUserId(1L);

        listener.onUserRegistered(event);

        ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);

        verify(emailService, times(1)).sendWelcomeEmail(emailCaptor.capture(), bodyCaptor.capture());

        assertEquals("test123@wp.pl", emailCaptor.getValue());
        assertTrue(bodyCaptor.getValue().contains("Kamil"));
    }

}