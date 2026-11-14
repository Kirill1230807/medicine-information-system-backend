package com.bank.appointmentservice.rabbitmq;

import com.bank.appointmentservice.entity.ProcessedEvent;
import com.bank.appointmentservice.messaging.MedicalEventListener;
import com.bank.appointmentservice.repository.ProcessedEventRepository;
import com.bank.appointmentservice.service.AppointmentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MedicalEventListenerTest {

    @Mock
    private ProcessedEventRepository processedEventRepository;

    @Mock
    private AppointmentService appointmentService;

    @InjectMocks
    private MedicalEventListener listener;

    @Test
    void onMedicalEvent_whenDuplicateEventArrives_processesOnlyOnce() {
        UUID eventId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        var event = new MedicalEventListener.IncomingMedicalEvent(
                eventId, "DOCTOR_DELETED", doctorId, null, Instant.now()
        );

        // Перший виклик: події ще немає в БД -> другий виклик: подія вже є
        when(processedEventRepository.existsById(eventId)).thenReturn(false, true);

        listener.onMedicalEvent(event);
        listener.onMedicalEvent(event); // Повторна доставка того самого повідомлення

        // Бізнес-логіка та збереження eventId виконані рівно 1 раз
        verify(appointmentService, times(1)).deleteByDoctorId(doctorId);
        verify(processedEventRepository, times(1)).save(any(ProcessedEvent.class));
    }
}