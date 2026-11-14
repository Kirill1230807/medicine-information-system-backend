package com.bank.userservice.event;

import com.bank.userservice.config.RabbitMqConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class MedicalEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleDomainEvent(MedicalDomainEvent event) {
        String routingKey = switch (event.eventType()) {
            case "DOCTOR_DELETED" -> "medical.doctor.deleted";
            case "PATIENT_DELETED" -> "medical.patient.deleted";
            case "DOCTOR_CABINET_UPDATED" -> "medical.doctor.updated";
            default -> "medical.general";
        };

        log.info("Publishing event after DB commit: type={}, eventId={}, entityId={}",
                event.eventType(), event.eventId(), event.entityId());

        rabbitTemplate.convertAndSend(RabbitMqConfig.MEDICAL_EXCHANGE, routingKey, event);
    }
}