package com.bank.userservice.client.resilience;

import com.bank.userservice.client.port.PatientNextAppointmentPort;
import com.bank.userservice.exception.AppointmentCircuitBreakerOpenException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Primary
@Component
public class ResilientPatientNextAppointmentDecorator implements PatientNextAppointmentPort {

    private final PatientNextAppointmentPort delegate;

    public ResilientPatientNextAppointmentDecorator(
            @Qualifier("patientNextAppointmentPort") PatientNextAppointmentPort delegate
    ) {
        this.delegate = delegate;
    }

    @Override
    @CircuitBreaker(name = "appointmentServiceCb", fallbackMethod = "getNextAppointmentFallback")
    @Retry(name = "appointmentServiceRetry")
    public NextAppointmentInfo getNextAppointment(UUID patientId) {
        return delegate.getNextAppointment(patientId);
    }

    @SuppressWarnings("unused")
    private NextAppointmentInfo getNextAppointmentFallback(UUID patientId, CallNotPermittedException ex) {
        throw new AppointmentCircuitBreakerOpenException(
                "Виклик getNextAppointment відхилено: Circuit Breaker для appointment-service перебуває у стані OPEN",
                ex
        );
    }
}