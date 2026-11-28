package com.bank.userservice.client.resilience;

import com.bank.userservice.client.port.AppointmentCountPort;
import com.bank.userservice.exception.AppointmentCircuitBreakerOpenException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component
public class ResilientAppointmentCountDecorator implements AppointmentCountPort {

    private final AppointmentCountPort delegate;

    public ResilientAppointmentCountDecorator(@Qualifier("appointmentCountPort") AppointmentCountPort delegate) {
        this.delegate = delegate;
    }

    @Override
    @CircuitBreaker(name = "appointmentServiceCb", fallbackMethod = "countAppointmentsFallback")
    @Retry(name = "appointmentServiceRetry")
    public long countAppointments() {
        return delegate.countAppointments();
    }

    @SuppressWarnings("unused")
    private long countAppointmentsFallback(CallNotPermittedException ex) {
        throw new AppointmentCircuitBreakerOpenException(
                "Виклик countAppointments відхилено: Circuit Breaker для appointment-service перебуває у стані OPEN",
                ex
        );
    }
}