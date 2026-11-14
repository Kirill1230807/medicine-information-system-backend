package com.bank.userservice.service;

import com.bank.userservice.entity.Doctor;
import com.bank.userservice.event.MedicalDomainEvent;
import com.bank.userservice.repository.DoctorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;
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
class DoctorServiceTest {

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private DoctorService doctorService;

    @Test
    void updateDoctor_whenDoctorExists_updatesDataAndPublishesCabinetUpdatedEvent() {
        UUID doctorId = UUID.randomUUID();
        Doctor existingDoctor = new Doctor();
        existingDoctor.setId(doctorId);
        existingDoctor.setSpecialization("Терапевт");
        existingDoctor.setCabinetNumber("101");

        Doctor updatedData = new Doctor();
        updatedData.setSpecialization("Кардіолог");
        updatedData.setCabinetNumber("305-Б");

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(existingDoctor));
        when(doctorRepository.save(any(Doctor.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Doctor result = doctorService.updateDoctor(doctorId, updatedData);

        assertEquals("Кардіолог", result.getSpecialization());
        assertEquals("305-Б", result.getCabinetNumber());

        ArgumentCaptor<MedicalDomainEvent> eventCaptor = ArgumentCaptor.forClass(MedicalDomainEvent.class);
        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());

        MedicalDomainEvent publishedEvent = eventCaptor.getValue();
        assertNotNull(publishedEvent.eventId());
        assertEquals("DOCTOR_CABINET_UPDATED", publishedEvent.eventType());
        assertEquals(doctorId, publishedEvent.entityId());
        assertEquals("305-Б", publishedEvent.payload());
    }

    @Test
    void updateDoctor_whenDoctorNotFound_throwsExceptionAndDoesNotPublishEvent() {
        UUID doctorId = UUID.randomUUID();
        Doctor updatedData = new Doctor();
        updatedData.setCabinetNumber("305-Б");

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> doctorService.updateDoctor(doctorId, updatedData));

        verify(doctorRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void deleteDoctor_whenDoctorExists_deletesDoctorAndPublishesDeletedEvent() {
        UUID doctorId = UUID.randomUUID();
        when(doctorRepository.existsById(doctorId)).thenReturn(true);

        doctorService.deleteDoctor(doctorId);

        verify(doctorRepository, times(1)).deleteById(doctorId);

        ArgumentCaptor<MedicalDomainEvent> eventCaptor = ArgumentCaptor.forClass(MedicalDomainEvent.class);
        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());

        MedicalDomainEvent publishedEvent = eventCaptor.getValue();
        assertNotNull(publishedEvent.eventId());
        assertEquals("DOCTOR_DELETED", publishedEvent.eventType());
        assertEquals(doctorId, publishedEvent.entityId());
        assertNull(publishedEvent.payload());
    }

    @Test
    void deleteDoctor_whenDoctorNotFound_throwsExceptionAndDoesNotPublishEvent() {
        UUID doctorId = UUID.randomUUID();
        when(doctorRepository.existsById(doctorId)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> doctorService.deleteDoctor(doctorId));

        verify(doctorRepository, never()).deleteById(any());
        verify(eventPublisher, never()).publishEvent(any());
    }
}