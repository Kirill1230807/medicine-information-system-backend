package com.bank.userservice.service;

import com.bank.userservice.event.MedicalDomainEvent;
import com.bank.userservice.repository.PatientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PatientServiceRabbitMqTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private PatientService patientService;

    @Test
    void deletePatient_whenPatientExists_deletesPatientAndPublishesDeletedEvent() {
        UUID patientId = UUID.randomUUID();
        when(patientRepository.existsById(patientId)).thenReturn(true);

        patientService.deletePatient(patientId);

        verify(patientRepository, times(1)).deleteById(patientId);

        ArgumentCaptor<MedicalDomainEvent> eventCaptor = ArgumentCaptor.forClass(MedicalDomainEvent.class);
        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());

        MedicalDomainEvent publishedEvent = eventCaptor.getValue();
        assertNotNull(publishedEvent.eventId());
        assertEquals("PATIENT_DELETED", publishedEvent.eventType());
        assertEquals(patientId, publishedEvent.entityId());
        assertNull(publishedEvent.payload());
    }

    @Test
    void deletePatient_whenPatientNotFound_throwsExceptionAndDoesNotPublishEvent() {
        UUID patientId = UUID.randomUUID();
        when(patientRepository.existsById(patientId)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> patientService.deletePatient(patientId));

        verify(patientRepository, never()).deleteById(any());
        verify(eventPublisher, never()).publishEvent(any());
    }
}