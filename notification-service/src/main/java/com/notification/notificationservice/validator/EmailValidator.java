package com.notification.notificationservice.validator;

import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;

public class EmailValidator {

    public static boolean isValidEmail(String email) {
        try {
            InternetAddress emailAddr = new InternetAddress(email);
            emailAddr.validate();
            return true;
        } catch (AddressException ex) {
            return false;
        }
    }
}
