package com.bank.userservice.service;

import com.bank.userservice.entity.IdempotencyRecord;
import com.bank.userservice.repository.IdempotencyRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.Optional;
import java.util.function.Supplier;

@Service
public class IdempotencyService {

    private static final Logger log = LoggerFactory.getLogger(IdempotencyService.class);

    private final IdempotencyRecordRepository repository;
    private final ObjectMapper objectMapper;

    public IdempotencyService(IdempotencyRecordRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public <T> ResponseEntity<String> executeIdempotent(
            String userId,
            String idempotencyKey,
            String endpoint,
            HttpStatus successStatus,
            Supplier<T> action
    ) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            T result = action.get();
            return ResponseEntity.status(successStatus)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(serialize(result));
        }

        Optional<IdempotencyRecord> existing = repository.findByUserIdAndIdempotencyKey(userId, idempotencyKey);
        if (existing.isPresent()) {
            IdempotencyRecord record = existing.get();
            log.info("Ідемпотентний повтор для userId={}, key={} -> повернення збереженої відповіді ({})",
                    userId, idempotencyKey, record.getHttpStatus());
            return ResponseEntity.status(record.getHttpStatus())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(record.getResponseBody());
        }

        T result = action.get();
        String responseBody = serialize(result);

        if (!successStatus.is5xxServerError()) {
            try {
                repository.saveAndFlush(new IdempotencyRecord(
                        userId,
                        idempotencyKey,
                        endpoint,
                        successStatus.value(),
                        responseBody
                ));
            } catch (DataIntegrityViolationException e) {
                return repository.findByUserIdAndIdempotencyKey(userId, idempotencyKey)
                        .map(rec -> ResponseEntity.status(rec.getHttpStatus())
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(rec.getResponseBody()))
                        .orElseThrow(() -> e);
            }
        }

        return ResponseEntity.status(successStatus)
                .contentType(MediaType.APPLICATION_JSON)
                .body(responseBody);
    }

    private String serialize(Object body) {
        try {
            return objectMapper.writeValueAsString(body);
        } catch (JacksonException e) {
            throw new IllegalStateException("Помилка серіалізації відповіді для ідемпотентності", e);
        }
    }
}