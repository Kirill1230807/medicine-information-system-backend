package com.bank.userservice.event;

import java.time.Instant;
import java.util.UUID;

public record MedicalDomainEvent(
        UUID eventId,
        String eventType,
        UUID entityId,
        String payload,
        Instant occurredAt
) {
    public static MedicalDomainEvent of(String eventType, UUID entityId, String payload) {
        return new MedicalDomainEvent(UUID.randomUUID(), eventType, entityId, payload, Instant.now());
    }
}