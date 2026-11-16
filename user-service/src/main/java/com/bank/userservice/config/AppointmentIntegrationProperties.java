package com.bank.userservice.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "services.appointment")
public record AppointmentIntegrationProperties(
        @NotBlank
        @Pattern(regexp = "^(?i)(http|grpc)$", message = "Невідомий протокол взаємодії: '${validatedValue}'. Дозволено лише 'http' або 'grpc'")
        String transport,

        @Valid @NotNull HttpProperties http,
        @Valid @NotNull GrpcProperties grpc
) {
    public record HttpProperties(
            @NotBlank String url,
            @NotNull Duration connectTimeout,
            @NotNull Duration readTimeout
    ) {}

    public record GrpcProperties(
            @NotBlank String host,
            @Positive int port,
            @NotNull Duration deadline
    ) {}
}