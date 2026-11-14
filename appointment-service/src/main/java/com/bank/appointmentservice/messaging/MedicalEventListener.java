package com.bank.appointmentservice.messaging;

import com.bank.appointmentservice.config.RabbitMqConsumerConfig;
import com.bank.appointmentservice.entity.ProcessedEvent;
import com.bank.appointmentservice.repository.ProcessedEventRepository;
import com.bank.appointmentservice.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class MedicalEventListener {

    private final ProcessedEventRepository processedEventRepository;
    private final AppointmentService appointmentService; // твій сервіс записів

    public record IncomingMedicalEvent(
            UUID eventId,
            String eventType,
            UUID entityId,
            String payload,
            Instant occurredAt
    ) {}

    @RabbitListener(queues = RabbitMqConsumerConfig.APPOINTMENT_QUEUE)
    @Transactional
    public void onMedicalEvent(IncomingMedicalEvent event) {
        // Перевірка ідемпотентності (Сценарій 5)
        if (processedEventRepository.existsById(event.eventId())) {
            log.warn("Duplicate event detected and ignored: eventId={}", event.eventId());
            return;
        }

        log.info("Processing event: type={}, eventId={}, entityId={}",
                event.eventType(), event.eventId(), event.entityId());

        switch (event.eventType()) {
            case "DOCTOR_DELETED" -> appointmentService.deleteByDoctorId(event.entityId());
            case "PATIENT_DELETED" -> appointmentService.deleteByPatientId(event.entityId());
            case "DOCTOR_CABINET_UPDATED" -> appointmentService.updateDoctorCabinet(event.entityId(), event.payload());
            default -> log.warn("Unknown event type: {}", event.eventType());
        }

        // Фіксуємо факт обробки події в тій самій транзакції
        processedEventRepository.save(new ProcessedEvent(event.eventId(), event.eventType()));
    }
}