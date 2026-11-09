package com.bank.appointmentservice.controller;

import com.bank.appointmentservice.client.UserServiceClient;
import com.bank.appointmentservice.dto.appointment.*;
import com.bank.appointmentservice.entity.Appointment;
import com.bank.appointmentservice.mapper.AppointmentMapper;
import com.bank.appointmentservice.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final UserServiceClient userServiceClient;
    private final AppointmentMapper appointmentMapper;

    public AppointmentController(AppointmentService appointmentService,
                                 UserServiceClient userServiceClient,
                                 AppointmentMapper appointmentMapper) {
        this.appointmentService = appointmentService;
        this.userServiceClient = userServiceClient;
        this.appointmentMapper = appointmentMapper;
    }

    // Повний список усіх записів - лише для адміністратора.
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<AppointmentResponseDTO> getAllAppointments() {
        return appointmentService.getAllAppointments().stream()
                .map(appointmentMapper::toDto)
                .collect(Collectors.toList());
    }

    // Для статистики в user-service
    @GetMapping("/count")
    @PreAuthorize("hasRole('ADMIN')")
    public long count() {
        return appointmentService.count();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') " +
            "or @appointmentSecurity.isOwnDoctor(#id, authentication) " +
            "or @appointmentSecurity.isOwnPatient(#id, authentication)")
    public AppointmentResponseDTO getAppointmentById(@PathVariable UUID id) {
        return appointmentMapper.toDto(appointmentService.getAppointmentById(id));
    }

    // адмін і лікар бачить записи пацієнта. пацієнт - лише свої
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR') or @patientSecurity.isSelf(#patientId, authentication)")
    public List<AppointmentResponseDTO> getAppointmentsByPatient(@PathVariable UUID patientId) {
        return appointmentService.getAppointmentsByPatient(patientId).stream()
                .map(appointmentMapper::toDto)
                .collect(Collectors.toList());
    }

    // розклад лікаря бачить адмін і сам цей лікар
    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasRole('ADMIN') or @doctorSecurity.isSelf(#doctorId, authentication)")
    public List<AppointmentResponseDTO> getAppointmentsByDoctor(@PathVariable UUID doctorId) {
        return appointmentService.getAppointmentsByDoctor(doctorId).stream()
                .map(appointmentMapper::toDto)
                .collect(Collectors.toList());
    }

    // адмін і лікар можуть призначити запис
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN') " +
            "or (hasRole('DOCTOR') and @doctorSecurity.isSelf(#dto.doctorId, authentication))")
    public AppointmentResponseDTO createAppointment(@Valid @RequestBody AppointmentCreateDTO dto) {
        userServiceClient.requireDoctor(dto.getDoctorId());
        userServiceClient.requirePatient(dto.getPatientId());

        Appointment appointment = appointmentMapper.toEntity(dto);
        return appointmentMapper.toDto(appointmentService.createAppointment(appointment));
    }

    // адмін і лікар можуть змінювати час, дату і статус запису
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('DOCTOR') and @appointmentSecurity.isOwnDoctor(#id, authentication))")
    public AppointmentResponseDTO updateAppointment(@PathVariable UUID id,
                                                    @Valid @RequestBody AppointmentCreateDTO dto) {
        Appointment updatedData = appointmentMapper.toEntity(dto);
        return appointmentMapper.toDto(appointmentService.updateAppointment(id, updatedData));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteAppointment(@PathVariable UUID id) {
        appointmentService.deleteAppointment(id);
    }

    // Їх викликає user-service при видаленні лікаря / пацієнта / користувача
    @DeleteMapping("/doctor/{doctorId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteByDoctor(@PathVariable UUID doctorId) {
        appointmentService.deleteByDoctor(doctorId);
    }

    @DeleteMapping("/patient/{patientId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteByPatient(@PathVariable UUID patientId) {
        appointmentService.deleteByPatient(patientId);
    }
}