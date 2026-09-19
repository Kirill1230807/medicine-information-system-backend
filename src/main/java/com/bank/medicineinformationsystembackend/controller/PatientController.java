package com.bank.medicineinformationsystembackend.controller;

import com.bank.medicineinformationsystembackend.dto.patient.*;
import com.bank.medicineinformationsystembackend.entity.Patient;
import com.bank.medicineinformationsystembackend.entity.User;
import com.bank.medicineinformationsystembackend.mapper.PatientMapper;
import com.bank.medicineinformationsystembackend.service.PatientService;
import com.bank.medicineinformationsystembackend.service.UserService;
import org.springframework.http.HttpStatus;
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
    private final PatientMapper patientMapper;

    public PatientController(PatientService patientService, UserService userService, PatientMapper patientMapper) {
        this.patientService = patientService;
        this.userService = userService;
        this.patientMapper = patientMapper;
    }

    @GetMapping
    public List getAllPatients() {
        return patientService.getAllPatients().stream()
                .map(patientMapper::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public PatientResponseDTO getPatientById(@PathVariable UUID id) {
        Patient patient = patientService.getPatientById(id);
        return patientMapper.toDto(patient);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public PatientResponseDTO createPatient(@RequestBody PatientCreateDTO dto) {
        Patient patient = patientMapper.toEntity(dto);

        User user = userService.getUserById(dto.getUserId());
        patient.setUser(user);

        Patient savedPatient = patientService.createPatient(patient);
        return patientMapper.toDto(savedPatient);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public PatientResponseDTO updatePatient(@PathVariable UUID id, @RequestBody PatientCreateDTO dto) {
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