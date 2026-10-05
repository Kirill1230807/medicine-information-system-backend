package com.bank.medicineinformationsystembackend.service;

import com.bank.medicineinformationsystembackend.entity.User;
import com.bank.medicineinformationsystembackend.repository.AppointmentRepository;
import com.bank.medicineinformationsystembackend.repository.DoctorRepository;
import com.bank.medicineinformationsystembackend.repository.PatientRepository;
import com.bank.medicineinformationsystembackend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class UserService {

    private static final Set<String> ASSIGNABLE_ROLES = Set.of("DOCTOR", "PATIENT");

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final KeycloakAdminService keycloakAdminService;

    public UserService(UserRepository userRepository,
                       DoctorRepository doctorRepository,
                       PatientRepository patientRepository,
                       AppointmentRepository appointmentRepository,
                       KeycloakAdminService keycloakAdminService) {
        this.userRepository = userRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.keycloakAdminService = keycloakAdminService;
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Користувача з ID " + id + " не знайдено"));
    }

    @Transactional
    public User createUser(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Користувач з email " + user.getEmail() + " вже існує");
        }
        return userRepository.save(user);
    }

    // видаляє користувача разом з усіма пов'язаними даними

    @Transactional
    public void deleteUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Користувача не знайдено"));

        if ("ROLE_ADMIN".equals(user.getRole())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Акаунт адміністратора видаляти не можна");
        }

        if ("ROLE_DOCTOR".equals(user.getRole())) {
            doctorRepository.findByUserId(id).ifPresent(doctor -> {
                appointmentRepository.deleteByDoctorId(doctor.getId());
                doctorRepository.delete(doctor);
            });
        } else if ("ROLE_PATIENT".equals(user.getRole())) {
            patientRepository.findByUserId(id).ifPresent(patient -> {
                appointmentRepository.deleteByPatientId(patient.getId());
                patientRepository.delete(patient);
            });
        }

        if (user.getExternalId() != null) {
            keycloakAdminService.deleteUser(user.getExternalId());
        }

        userRepository.delete(user);
    }

    @Transactional(readOnly = true)
    public Optional getUserByExternalId(String externalId) {
        return userRepository.findByExternalId(externalId);
    }

    @Transactional(readOnly = true)
    public Optional getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Синхронізує користувача з Keycloak: створює запис при першому вході
     * і оновлює роль/email/ім'я при наступних. Джерело істини для ролей і
     * персональних даних - Keycloak (заповнюються при реєстрації).
     */
    @Transactional
    public User syncOidcUser(String externalId, String email, String firstName, String lastName, String role) {
        User user = userRepository.findByExternalId(externalId)
                // якщо Keycloak перестворили, "sub" змінився, а email той самий
                .or(() -> email != null ? userRepository.findByEmail(email) : Optional.<User>empty())
                .orElseGet(User::new);

        user.setExternalId(externalId);
        if (email != null) {
            user.setEmail(email);
        }
        if (firstName != null) {
            user.setFirstName(firstName);
        }
        if (lastName != null) {
            user.setLastName(lastName);
        }
        user.setRole(role);
        return userRepository.save(user);
    }

    public User assignRole(UUID id, String role) {
        if (role == null || !ASSIGNABLE_ROLES.contains(role)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Дозволені ролі: DOCTOR, PATIENT");
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Користувача не знайдено"));

        if ("ROLE_ADMIN".equals(user.getRole())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Роль адміністратора змінювати не можна");
        }
        if (user.getExternalId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Користувач не пов'язаний з Keycloak");
        }

        keycloakAdminService.assignRealmRole(user.getExternalId(), role);

        user.setRole("ROLE_" + role);
        return userRepository.save(user);
    }
}