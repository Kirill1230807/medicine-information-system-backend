package com.bank.medicineinformationsystembackend.controller;

import com.bank.medicineinformationsystembackend.dto.patient.*;
import com.bank.medicineinformationsystembackend.entity.Patient;
import com.bank.medicineinformationsystembackend.entity.User;
import com.bank.medicineinformationsystembackend.mapper.PatientMapper;
import com.bank.medicineinformationsystembackend.service.CurrentUserService;
import com.bank.medicineinformationsystembackend.service.PatientService;
import com.bank.medicineinformationsystembackend.service.UserService;
import com.bank.medicineinformationsystembackend.validation.annotation.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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

    public PatientController(PatientService patientService, UserService userService,
                             CurrentUserService currentUserService, PatientMapper patientMapper) {
        this.patientService = patientService;
        this.userService = userService;
        this.currentUserService = currentUserService;
        this.patientMapper = patientMapper;
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
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public PatientResponseDTO createPatient(@Valid @RequestBody PatientCreateDTO dto) {
        Patient patient = patientMapper.toEntity(dto);

        User user = userService.getUserById(dto.getUserId());
        patient.setUser(user);

        Patient savedPatient = patientService.createPatient(patient);
        return patientMapper.toDto(savedPatient);
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
}