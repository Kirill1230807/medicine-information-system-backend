package com.bank.userservice.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ResilienceEventLogger {

    private static final Logger log = LoggerFactory.getLogger(ResilienceEventLogger.class);

    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RetryRegistry retryRegistry;

    public ResilienceEventLogger(CircuitBreakerRegistry circuitBreakerRegistry, RetryRegistry retryRegistry) {
        this.circuitBreakerRegistry = circuitBreakerRegistry;
        this.retryRegistry = retryRegistry;
    }

    @PostConstruct
    public void registerEventListeners() {
        circuitBreakerRegistry.circuitBreaker("appointmentServiceCb")
                .getEventPublisher()
                .onStateTransition(event -> log.warn(
                        "[CircuitBreaker: {}] Зміна стану: {} -> {}",
                        event.getCircuitBreakerName(),
                        event.getStateTransition().getFromState(),
                        event.getStateTransition().getToState()
                ))
                .onCallNotPermitted(event -> log.warn(
                        "[CircuitBreaker: {}] Виклик відхилено (стан OPEN)",
                        event.getCircuitBreakerName()
                ));

        retryRegistry.retry("appointmentServiceRetry")
                .getEventPublisher()
                .onRetry(event -> log.warn(
                        "[Retry: {}] Повторна спроба #{} після помилки: {}",
                        event.getName(),
                        event.getNumberOfRetryAttempts(),
                        event.getLastThrowable() != null ? event.getLastThrowable().toString() : "unknown"
                ));
    }
}