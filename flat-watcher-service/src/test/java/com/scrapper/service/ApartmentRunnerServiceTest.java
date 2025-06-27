package com.scrapper.service;

import com.scrapper.repository.ApartmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.lang.reflect.Method;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApartmentRunnerServiceTest {

    @Mock
    private WebDriver mockWebDriver;

    @Mock
    private ApartmentRepository mockRepository;

    @InjectMocks
    private ApartmentRunnerService service;

    @Test
    void testPrivateGetPrice() throws Exception {
        WebElement mockCard = mock(WebElement.class);
        WebElement priceElement = mock(WebElement.class);

        when(mockCard.findElement(By.cssSelector(".evk7nst0"))).thenReturn(priceElement);
        when(priceElement.getText()).thenReturn("425 000 zł");

        Method method = ApartmentRunnerService.class.getDeclaredMethod("getPrice", WebElement.class);
        method.setAccessible(true);

        BigDecimal result = (BigDecimal) method.invoke(service, mockCard);

        assertEquals(new BigDecimal("425000"), result);
    }

}