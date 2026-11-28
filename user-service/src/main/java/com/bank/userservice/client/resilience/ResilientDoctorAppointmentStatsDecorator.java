package com.bank.userservice.client.resilience;

import com.bank.userservice.client.port.DoctorAppointmentStatsPort;
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
public class ResilientDoctorAppointmentStatsDecorator implements DoctorAppointmentStatsPort {

    private final DoctorAppointmentStatsPort delegate;

    public ResilientDoctorAppointmentStatsDecorator(
            @Qualifier("doctorAppointmentStatsPort") DoctorAppointmentStatsPort delegate
    ) {
        this.delegate = delegate;
    }

    @Override
    @CircuitBreaker(name = "appointmentServiceCb", fallbackMethod = "getActiveAppointmentsCountFallback")
    @Retry(name = "appointmentServiceRetry")
    public long getActiveAppointmentsCount(UUID doctorId) {
        return delegate.getActiveAppointmentsCount(doctorId);
    }

    @SuppressWarnings("unused")
    private long getActiveAppointmentsCountFallback(UUID doctorId, CallNotPermittedException ex) {
        throw new AppointmentCircuitBreakerOpenException(
                "Виклик getActiveAppointmentsCount відхилено: Circuit Breaker для appointment-service перебуває у стані OPEN",
                ex
        );
    }
}