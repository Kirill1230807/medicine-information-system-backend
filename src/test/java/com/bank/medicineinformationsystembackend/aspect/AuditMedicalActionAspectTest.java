package com.bank.medicineinformationsystembackend.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;

class AuditMedicalActionAspectTest {

    private AuditMedicalActionAspect aspect;
    private JoinPoint joinPointMock;

    @BeforeEach
    void setUp() {
        aspect = new AuditMedicalActionAspect();

        joinPointMock = Mockito.mock(JoinPoint.class);
        Signature signatureMock = Mockito.mock(Signature.class);

        when(signatureMock.getName()).thenReturn("createPatient");
        when(joinPointMock.getSignature()).thenReturn(signatureMock);
        when(joinPointMock.getArgs()).thenReturn(new Object[]{"John Doe", 30});
    }

    @Test
    void shouldExtractMethodNameAndArgs() {
        // WHEN
        aspect.auditAction(joinPointMock);

        // THEN: перевіряємо фактичну логіку отримання даних з JoinPoint
        verify(joinPointMock, times(1)).getSignature();
        verify(joinPointMock, times(1)).getArgs();
    }
}