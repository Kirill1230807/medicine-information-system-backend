package com.bank.medicineinformationsystembackend.service;

import com.bank.medicineinformationsystembackend.entity.Patient;
import com.bank.medicineinformationsystembackend.entity.User;
import com.bank.medicineinformationsystembackend.repository.PatientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private PatientService patientService;

    @Test
    void getAllPatients_returnsListOfPatients() {
        // Arrange
        Patient patient = new Patient();
        UUID id = UUID.randomUUID();
        patient.setId(id);
        when(patientRepository.findAll()).thenReturn(List.of(patient));

        // Act
        List<Patient> result = patientService.getAllPatients();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(id, result.getFirst().getId());
        verify(patientRepository, times(1)).findAll();
    }

    @Test
    void getPatientById_existingId_returnsPatient() {
        // Arrange
        UUID id = UUID.randomUUID();
        Patient patient = new Patient();
        patient.setId(id);
        when(patientRepository.findById(id)).thenReturn(Optional.of(patient));

        // Act
        Patient result = patientService.getPatientById(id);

        // Assert
        assertNotNull(result);
        assertEquals(id, result.getId());
        verify(patientRepository, times(1)).findById(id);
    }

    @Test
    void getPatientById_nonExistingId_throwsRuntimeException() {
        // Arrange
        UUID id = UUID.randomUUID();
        when(patientRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> patientService.getPatientById(id)
        );
        assertTrue(exception.getMessage().contains("не знайдено"));
        verify(patientRepository, times(1)).findById(id);
    }

    @Test
    void createPatient_validData_returnsSavedPatient() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setFirstName("Іван");
        user.setLastName("Петренко");

        Patient patient = new Patient();
        patient.setUser(user);

        when(patientRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(patientRepository.save(any(Patient.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Patient result = patientService.createPatient(patient);

        // Assert
        assertNotNull(result);
        assertEquals("Іван", result.getFirstName());
        assertEquals("Петренко", result.getLastName());
        verify(patientRepository, times(1)).findByUserId(userId);
        verify(patientRepository, times(1)).save(patient);
    }

    @Test
    void createPatient_profileAlreadyExists_throwsRuntimeException() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        Patient patient = new Patient();
        patient.setUser(user);

        when(patientRepository.findByUserId(userId)).thenReturn(Optional.of(new Patient()));

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> patientService.createPatient(patient)
        );
        assertEquals("Профіль пацієнта для цього користувача вже існує", exception.getMessage());
        verify(patientRepository, times(1)).findByUserId(userId);
        verify(patientRepository, never()).save(any());
    }

    @Test
    void createPatient_missingFirstOrLastName_throwsResponseStatusException() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setFirstName(""); // Невалідні дані (порожнє ім'я)
        user.setLastName("Петренко");

        Patient patient = new Patient();
        patient.setUser(user);

        when(patientRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> patientService.createPatient(patient)
        );
        assertTrue(exception.getReason().contains("У обраного користувача немає імені та прізвища"));
        verify(patientRepository, times(1)).findByUserId(userId);
        verify(patientRepository, never()).save(any());
    }

    @Test
    void updatePatient_existingId_updatesAndReturnsPatient() {
        // Arrange
        UUID id = UUID.randomUUID();
        Patient existingPatient = new Patient();
        existingPatient.setId(id);
        existingPatient.setPhoneNumber("111111");

        Patient updatedData = new Patient();
        updatedData.setPhoneNumber("999999");
        updatedData.setDateOfBirth(LocalDate.of(2000, 1, 1));

        when(patientRepository.findById(id)).thenReturn(Optional.of(existingPatient));
        when(patientRepository.save(any(Patient.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Patient result = patientService.updatePatient(id, updatedData);

        // Assert
        assertNotNull(result);
        assertEquals("999999", result.getPhoneNumber());
        assertEquals(LocalDate.of(2000, 1, 1), result.getDateOfBirth());
        verify(patientRepository, times(1)).findById(id);
        verify(patientRepository, times(1)).save(existingPatient);
    }

    @Test
    void deletePatient_existingId_deletesSuccessfully() {
        // Arrange
        UUID id = UUID.randomUUID();
        when(patientRepository.existsById(id)).thenReturn(true);
        doNothing().when(patientRepository).deleteById(id);

        // Act & Assert
        assertDoesNotThrow(() -> patientService.deletePatient(id));
        verify(patientRepository, times(1)).existsById(id);
        verify(patientRepository, times(1)).deleteById(id);
    }

    @Test
    void deletePatient_nonExistingId_throwsRuntimeException() {
        // Arrange
        UUID id = UUID.randomUUID();
        when(patientRepository.existsById(id)).thenReturn(false);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> patientService.deletePatient(id));
        verify(patientRepository, times(1)).existsById(id);
        verify(patientRepository, never()).deleteById(any());
    }

    @Test
    void getPatientByUserId_existingUserId_returnsPatient() {
        // Arrange
        UUID userId = UUID.randomUUID();
        Patient patient = new Patient();
        when(patientRepository.findByUserId(userId)).thenReturn(Optional.of(patient));

        // Act
        Patient result = patientService.getPatientByUserId(userId);

        // Assert
        assertNotNull(result);
        verify(patientRepository, times(1)).findByUserId(userId);
    }

    @Test
    void getPatientByUserId_nonExistingUserId_throwsResponseStatusException() {
        // Arrange
        UUID userId = UUID.randomUUID();
        when(patientRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> patientService.getPatientByUserId(userId));
        verify(patientRepository, times(1)).findByUserId(userId);
    }
}