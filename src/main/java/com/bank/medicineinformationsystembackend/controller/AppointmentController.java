package com.bank.medicineinformationsystembackend.controller;

import com.bank.medicineinformationsystembackend.dto.appointment.*;
import com.bank.medicineinformationsystembackend.entity.Appointment;
import com.bank.medicineinformationsystembackend.entity.Doctor;
import com.bank.medicineinformationsystembackend.entity.Patient;
import com.bank.medicineinformationsystembackend.mapper.AppointmentMapper;
import com.bank.medicineinformationsystembackend.service.AppointmentService;
import com.bank.medicineinformationsystembackend.service.DoctorService;
import com.bank.medicineinformationsystembackend.service.PatientService;
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
    private final DoctorService doctorService;
    private final PatientService patientService;
    private final AppointmentMapper appointmentMapper;

    public AppointmentController(AppointmentService appointmentService,
                                 DoctorService doctorService,
                                 PatientService patientService,
                                 AppointmentMapper appointmentMapper) {
        this.appointmentService = appointmentService;
        this.doctorService = doctorService;
        this.patientService = patientService;
        this.appointmentMapper = appointmentMapper;
    }

    // Повний список усіх записів - лише для адміністратора.
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List getAllAppointments() {
        return appointmentService.getAllAppointments().stream()
                .map(appointmentMapper::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') " +
            "or @appointmentSecurity.isOwnDoctor(#id, authentication) " +
            "or @appointmentSecurity.isOwnPatient(#id, authentication)")
    public AppointmentResponseDTO getAppointmentById(@PathVariable UUID id) {
        Appointment appointment = appointmentService.getAppointmentById(id);
        return appointmentMapper.toDto(appointment);
    }

    // адмін і лікар бачить записи пацієнта. пацієнт - лише свої
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR') or @patientSecurity.isSelf(#patientId, authentication)")
    public List getAppointmentsByPatient(@PathVariable UUID patientId) {
        return appointmentService.getAppointmentsByPatient(patientId).stream()
                .map(appointmentMapper::toDto)
                .collect(Collectors.toList());
    }

    // розклад лікаря бачить адмін і сам цей лікар
    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasRole('ADMIN') or @doctorSecurity.isSelf(#doctorId, authentication)")
    public List getAppointmentsByDoctor(@PathVariable UUID doctorId) {
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
        Appointment appointment = appointmentMapper.toEntity(dto);

        Doctor doctor = doctorService.getDoctorById(dto.getDoctorId());
        Patient patient = patientService.getPatientById(dto.getPatientId());

        appointment.setDoctor(doctor);
        appointment.setPatient(patient);

        Appointment savedAppointment = appointmentService.createAppointment(appointment);
        return appointmentMapper.toDto(savedAppointment);
    }

    // адмін і лікар можуть змінювати час, дату і статус запису
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('DOCTOR') and @appointmentSecurity.isOwnDoctor(#id, authentication))")
    public AppointmentResponseDTO updateAppointment(@PathVariable UUID id, @Valid @RequestBody AppointmentCreateDTO dto) {
        Appointment updatedData = appointmentMapper.toEntity(dto);
        Appointment savedAppointment = appointmentService.updateAppointment(id, updatedData);
        return appointmentMapper.toDto(savedAppointment);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteAppointment(@PathVariable UUID id) {
        appointmentService.deleteAppointment(id);
    }
}