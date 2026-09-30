package com.bank.medicineinformationsystembackend.service;

import com.bank.medicineinformationsystembackend.entity.Doctor;
import com.bank.medicineinformationsystembackend.entity.Patient;
import com.bank.medicineinformationsystembackend.entity.User;
import com.bank.medicineinformationsystembackend.repository.AppointmentRepository;
import com.bank.medicineinformationsystembackend.repository.DoctorRepository;
import com.bank.medicineinformationsystembackend.repository.PatientRepository;
import com.bank.medicineinformationsystembackend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private KeycloakAdminService keycloakAdminService;

    @InjectMocks
    private UserService userService;

    @Test
    void getAllUsers_returnsListOfUsers() {
        // Arrange
        User user = new User();
        UUID id = UUID.randomUUID();
        user.setId(id);
        when(userRepository.findAll()).thenReturn(List.of(user));

        // Act
        List<User> result = userService.getAllUsers();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(id, result.get(0).getId());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void getUserById_existingId_returnsUser() {
        // Arrange
        UUID id = UUID.randomUUID();
        User user = new User();
        user.setId(id);
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        // Act
        User result = userService.getUserById(id);

        // Assert
        assertNotNull(result);
        assertEquals(id, result.getId());
        verify(userRepository, times(1)).findById(id);
    }

    @Test
    void getUserById_nonExistingId_throwsRuntimeException() {
        // Arrange
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userService.getUserById(id)
        );
        assertTrue(exception.getMessage().contains("не знайдено"));
        verify(userRepository, times(1)).findById(id);
    }

    @Test
    void createUser_uniqueEmail_returnsSavedUser() {
        // Arrange
        User user = new User();
        user.setEmail("test@bank.com");

        when(userRepository.findByEmail("test@bank.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User result = userService.createUser(user);

        // Assert
        assertNotNull(result);
        assertEquals("test@bank.com", result.getEmail());
        verify(userRepository, times(1)).findByEmail("test@bank.com");
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void createUser_existingEmail_throwsRuntimeException() {
        // Arrange
        User user = new User();
        user.setEmail("existing@bank.com");

        when(userRepository.findByEmail("existing@bank.com")).thenReturn(Optional.of(new User()));

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userService.createUser(user)
        );
        assertTrue(exception.getMessage().contains("вже існує"));
        verify(userRepository, times(1)).findByEmail("existing@bank.com");
        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteUser_adminRole_throwsResponseStatusException() {
        // Arrange
        UUID id = UUID.randomUUID();
        User admin = new User();
        admin.setId(id);
        admin.setRole("ROLE_ADMIN");

        when(userRepository.findById(id)).thenReturn(Optional.of(admin));

        // Act & Assert
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> userService.deleteUser(id)
        );
        assertTrue(exception.getReason().contains("Акаунт адміністратора видаляти не можна"));
        verify(userRepository, times(1)).findById(id);
        verify(userRepository, never()).delete(any());
    }

    @Test
    void deleteUser_doctorRole_deletesDoctorAppointmentsAndKeycloakAccount() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        User doctorUser = new User();
        doctorUser.setId(userId);
        doctorUser.setRole("ROLE_DOCTOR");
        doctorUser.setExternalId("ext-doctor-123");

        Doctor doctor = new Doctor();
        doctor.setId(doctorId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(doctorUser));
        when(doctorRepository.findByUserId(userId)).thenReturn(Optional.of(doctor));
        doNothing().when(appointmentRepository).deleteByDoctorId(doctorId);
        doNothing().when(doctorRepository).delete(doctor);
        doNothing().when(keycloakAdminService).deleteUser("ext-doctor-123");
        doNothing().when(userRepository).delete(doctorUser);

        // Act
        assertDoesNotThrow(() -> userService.deleteUser(userId));

        // Assert
        verify(userRepository, times(1)).findById(userId);
        verify(doctorRepository, times(1)).findByUserId(userId);
        verify(appointmentRepository, times(1)).deleteByDoctorId(doctorId);
        verify(doctorRepository, times(1)).delete(doctor);
        verify(keycloakAdminService, times(1)).deleteUser("ext-doctor-123");
        verify(userRepository, times(1)).delete(doctorUser);
    }

    @Test
    void deleteUser_patientRole_deletesPatientAppointmentsAndKeycloakAccount() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        User patientUser = new User();
        patientUser.setId(userId);
        patientUser.setRole("ROLE_PATIENT");
        patientUser.setExternalId("ext-patient-123");

        Patient patient = new Patient();
        patient.setId(patientId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(patientUser));
        when(patientRepository.findByUserId(userId)).thenReturn(Optional.of(patient));
        doNothing().when(appointmentRepository).deleteByPatientId(patientId);
        doNothing().when(patientRepository).delete(patient);
        doNothing().when(keycloakAdminService).deleteUser("ext-patient-123");
        doNothing().when(userRepository).delete(patientUser);

        // Act
        assertDoesNotThrow(() -> userService.deleteUser(userId));

        // Assert
        verify(userRepository, times(1)).findById(userId);
        verify(patientRepository, times(1)).findByUserId(userId);
        verify(appointmentRepository, times(1)).deleteByPatientId(patientId);
        verify(patientRepository, times(1)).delete(patient);
        verify(keycloakAdminService, times(1)).deleteUser("ext-patient-123");
        verify(userRepository, times(1)).delete(patientUser);
    }

    @Test
    void getUserByExternalId_returnsOptionalUser() {
        // Arrange
        String extId = "ext-123";
        User user = new User();
        when(userRepository.findByExternalId(extId)).thenReturn(Optional.of(user));

        // Act
        Optional result = userService.getUserByExternalId(extId);

        // Assert
        assertTrue(result.isPresent());
        verify(userRepository, times(1)).findByExternalId(extId);
    }

    @Test
    void getUserByEmail_returnsOptionalUser() {
        // Arrange
        String email = "mail@bank.com";
        User user = new User();
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // Act
        Optional result = userService.getUserByEmail(email);

        // Assert
        assertTrue(result.isPresent());
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    void syncOidcUser_existingByExternalId_updatesAndSaves() {
        // Arrange
        String extId = "ext-123";
        User existingUser = new User();
        existingUser.setExternalId(extId);

        when(userRepository.findByExternalId(extId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User result = userService.syncOidcUser(extId, "new@bank.com", "Іван", "Коваль", "ROLE_PATIENT");

        // Assert
        assertNotNull(result);
        assertEquals("new@bank.com", result.getEmail());
        assertEquals("Іван", result.getFirstName());
        assertEquals("Коваль", result.getLastName());
        assertEquals("ROLE_PATIENT", result.getRole());
        verify(userRepository, times(1)).findByExternalId(extId);
        verify(userRepository, times(1)).save(existingUser);
    }

    @Test
    void assignRole_validRole_updatesInKeycloakAndDb() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setRole("ROLE_PATIENT");
        user.setExternalId("ext-123");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        doNothing().when(keycloakAdminService).assignRealmRole("ext-123", "DOCTOR");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User result = userService.assignRole(userId, "DOCTOR");

        // Assert
        assertNotNull(result);
        assertEquals("ROLE_DOCTOR", result.getRole());
        verify(keycloakAdminService, times(1)).assignRealmRole("ext-123", "DOCTOR");
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void assignRole_invalidRole_throwsResponseStatusException() {
        // Act & Assert
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> userService.assignRole(UUID.randomUUID(), "INVALID_ROLE")
        );
        assertTrue(exception.getReason().contains("Дозволені ролі: DOCTOR, PATIENT"));
        verifyNoInteractions(userRepository);
        verifyNoInteractions(keycloakAdminService);
    }

    @Test
    void assignRole_adminUser_throwsResponseStatusException() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User admin = new User();
        admin.setId(userId);
        admin.setRole("ROLE_ADMIN");
        admin.setExternalId("ext-admin");

        when(userRepository.findById(userId)).thenReturn(Optional.of(admin));

        // Act & Assert
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> userService.assignRole(userId, "DOCTOR")
        );
        assertTrue(exception.getReason().contains("Роль адміністратора змінювати не можна"));
        verify(keycloakAdminService, never()).assignRealmRole(any(), any());
    }
}