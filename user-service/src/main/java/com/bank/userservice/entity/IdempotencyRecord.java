package com.bank.userservice.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "idempotency_records")
public class IdempotencyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "idempotency_key", nullable = false)
    private String idempotencyKey;

    @Column(name = "endpoint", nullable = false)
    private String endpoint;

    @Column(name = "http_status", nullable = false)
    private int httpStatus;

    @Column(name = "response_body", nullable = false, columnDefinition = "TEXT")
    private String responseBody;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public IdempotencyRecord() {}

    public IdempotencyRecord(String userId, String idempotencyKey, String endpoint, int httpStatus, String responseBody) {
        this.userId = userId;
        this.idempotencyKey = idempotencyKey;
        this.endpoint = endpoint;
        this.httpStatus = httpStatus;
        this.responseBody = responseBody;
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public String getUserId() { return userId; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public String getEndpoint() { return endpoint; }
    public int getHttpStatus() { return httpStatus; }
    public String getResponseBody() { return responseBody; }
    public Instant getCreatedAt() { return createdAt; }
}