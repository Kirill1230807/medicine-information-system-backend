package com.bank.medicineinformationsystembackend.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.dao.OptimisticLockingFailureException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class RetryOnOptimisticLockAspectTest {

    private RetryOnOptimisticLockAspect aspect;
    private ProceedingJoinPoint joinPointMock;

    @BeforeEach
    void setUp() {
        // GIVEN: Клас аспекту без підняття контексту Spring
        aspect = new RetryOnOptimisticLockAspect();

        // Створюється mock-об'єкт для ProceedingJoinPoint
        joinPointMock = Mockito.mock(ProceedingJoinPoint.class);

        Signature signatureMock = Mockito.mock(Signature.class);
        when(signatureMock.getName()).thenReturn("updatePatient");
        when(joinPointMock.getSignature()).thenReturn(signatureMock);
    }

    @Test
    void shouldProceedSuccessfullyOnFirstAttempt() throws Throwable {
        // GIVEN
        when(joinPointMock.proceed()).thenReturn("Success");

        // WHEN
        Object result = aspect.retry(joinPointMock);

        // THEN: Тест перевіряє, що proceed() викликаний один раз
        assertEquals("Success", result);
        verify(joinPointMock, times(1)).proceed();
    }

    @Test
    void shouldRetryAndSucceedOnSecondAttempt() throws Throwable {
        // GIVEN: Перша спроба - виняток блокування, друга - успіх
        when(joinPointMock.proceed())
                .thenThrow(new OptimisticLockingFailureException("Conflict"))
                .thenReturn("Success After Retry");

        // WHEN
        Object result = aspect.retry(joinPointMock);

        // THEN: proceed() викликано 2 рази
        assertEquals("Success After Retry", result);
        verify(joinPointMock, times(2)).proceed();
    }

    @Test
    void shouldThrowExceptionAfterMaxRetries() throws Throwable {
        // GIVEN: Всі спроби кидають виняток
        when(joinPointMock.proceed()).thenThrow(new OptimisticLockingFailureException("Conflict"));

        // WHEN & THEN: після 3 спроб виняток має бути прокинутий далі
        assertThrows(OptimisticLockingFailureException.class, () -> aspect.retry(joinPointMock));
        verify(joinPointMock, times(3)).proceed();
    }
}