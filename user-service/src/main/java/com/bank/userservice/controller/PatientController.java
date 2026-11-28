package com.bank.userservice.controller;

import com.bank.userservice.client.port.PatientNextAppointmentPort;
import com.bank.userservice.dto.patient.*;
import com.bank.userservice.entity.Patient;
import com.bank.userservice.entity.User;
import com.bank.userservice.mapper.PatientMapper;
import com.bank.userservice.service.CurrentUserService;
import com.bank.userservice.service.IdempotencyService;
import com.bank.userservice.service.PatientService;
import com.bank.userservice.service.UserService;
import com.bank.userservice.validation.annotation.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;
    private final UserService userService;
    private final CurrentUserService currentUserService;
    private final PatientMapper patientMapper;
    private final PatientNextAppointmentPort patientNextAppointmentPort;
    private final IdempotencyService idempotencyService;

    public PatientController(PatientService patientService, UserService userService,
                             CurrentUserService currentUserService, PatientMapper patientMapper, PatientNextAppointmentPort patientNextAppointmentPort, IdempotencyService idempotencyService) {
        this.patientService = patientService;
        this.userService = userService;
        this.currentUserService = currentUserService;
        this.patientMapper = patientMapper;
        this.patientNextAppointmentPort = patientNextAppointmentPort;
        this.idempotencyService = idempotencyService;
    }

    // Повний список пацієнтів бачать лише персонал (адмін, лікар).
    // Пацієнту чужі особисті дані не показуються.
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    public List getAllPatients() {
        return patientService.getAllPatients().stream()
                .map(patientMapper::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/me")
    public PatientResponseDTO getMyPatientProfile(@AuthenticatedUser User user) {
        Patient patient = patientService.getPatientByUserId(user.getId());
        return patientMapper.toDto(patient);
    }

    // Персонал бачить будь-якого пацієнта; пацієнт - лише самого себе.
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR') or @patientSecurity.isSelf(#id, authentication)")
    public PatientResponseDTO getPatientById(@PathVariable UUID id) {
        Patient patient = patientService.getPatientById(id);
        return patientMapper.toDto(patient);
    }

    @PostMapping
    public ResponseEntity<String> createPatient(
            @AuthenticatedUser User user,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody PatientCreateDTO dto
    ) {
        return idempotencyService.executeIdempotent(
                user.getId().toString(),
                idempotencyKey,
                "POST /api/patients",
                HttpStatus.CREATED,
                () -> {
                    Patient patient = patientMapper.toEntity(dto);
                    // Викличте ваш метод створення з PatientService (наприклад, createPatient(user, patient) або createPatient(patient))
                    Patient saved = patientService.createPatient(user, patient);
                    return patientMapper.toDto(saved);
                }
        );
    }

    // Адмін може редагувати будь-якого пацієнта; сам пацієнт - тільки власні дані
    // (телефон і дату народження), а не чужі.
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @patientSecurity.isSelf(#id, authentication)")
    public PatientResponseDTO updatePatient(@PathVariable UUID id, @Valid @RequestBody PatientCreateDTO dto) {
        Patient updatedData = patientMapper.toEntity(dto);
        Patient savedPatient = patientService.updatePatient(id, updatedData);
        return patientMapper.toDto(savedPatient);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deletePatient(@PathVariable UUID id) {
        patientService.deletePatient(id);
    }

    @GetMapping("/{id}/internal-test")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR') or @patientSecurity.isSelf(#id, authentication)")
    public PatientResponseDTO getPatientByIdInternalTest(@PathVariable UUID id) {
        Patient patient = patientService.getPatientByIdInternal(id);
        return patientMapper.toDto(patient);
    }

    @GetMapping("/{id}/next-appointment")
    public PatientNextAppointmentPort.NextAppointmentInfo getPatientNextAppointment(@PathVariable UUID id) {
        return patientNextAppointmentPort.getNextAppointment(id);
    }
}