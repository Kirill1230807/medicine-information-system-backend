package com.bank.medicineinformationsystembackend.controller;

import com.bank.medicineinformationsystembackend.dto.doctor.*;
import com.bank.medicineinformationsystembackend.entity.Doctor;
import com.bank.medicineinformationsystembackend.entity.User;
import com.bank.medicineinformationsystembackend.mapper.DoctorMapper;
import com.bank.medicineinformationsystembackend.service.DoctorService;
import com.bank.medicineinformationsystembackend.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    private final DoctorService doctorService;
    private final UserService userService;
    private final DoctorMapper doctorMapper;

    public DoctorController(DoctorService doctorService, UserService userService, DoctorMapper doctorMapper) {
        this.doctorService = doctorService;
        this.userService = userService;
        this.doctorMapper = doctorMapper;
    }

    @GetMapping
    public List getAllDoctors() {
        return doctorService.getAllDoctors().stream()
                .map(doctorMapper::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public DoctorResponseDTO getDoctorById(@PathVariable UUID id) {
        Doctor doctor = doctorService.getDoctorById(id);
        return doctorMapper.toDto(doctor);
    }

    @GetMapping("/specialization/{specialization}")
    public List getDoctorsBySpecialization(@PathVariable String specialization) {
        return doctorService.getDoctorsBySpecialization(specialization).stream()
                .map(doctorMapper::toDto)
                .collect(Collectors.toList());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public DoctorResponseDTO createDoctor(@RequestBody DoctorCreateDTO dto) {
        Doctor doctor = doctorMapper.toEntity(dto);

        User user = userService.getUserById(dto.getUserId());
        doctor.setUser(user);

        Doctor savedDoctor = doctorService.createDoctor(doctor);
        return doctorMapper.toDto(savedDoctor);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public DoctorResponseDTO updateDoctor(@PathVariable UUID id, @RequestBody DoctorCreateDTO dto) {
        Doctor updatedData = doctorMapper.toEntity(dto);
        Doctor savedDoctor = doctorService.updateDoctor(id, updatedData);
        return doctorMapper.toDto(savedDoctor);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteDoctor(@PathVariable UUID id) {
        doctorService.deleteDoctor(id);
    }
}