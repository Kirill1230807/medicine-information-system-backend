package com.bank.appointmentservice.controller;

import com.bank.appointmentservice.dto.DoctorAppointmentsStatsDTO;
import com.bank.appointmentservice.dto.NextAppointmentDTO;
import com.bank.appointmentservice.service.AppointmentQueryDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentInternalController {

    private final AppointmentQueryDomainService queryService;

    @GetMapping("/count")
    public long countAppointments() {
        return queryService.countAll();
    }

    @GetMapping("/doctor/{doctorId}/stats")
    public DoctorAppointmentsStatsDTO getDoctorStats(@PathVariable UUID doctorId) {
        return queryService.findDoctorStats(doctorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Лікаря або записів не знайдено"));
    }

    @GetMapping("/patient/{patientId}/next")
    public NextAppointmentDTO getNextPatientAppointment(@PathVariable UUID patientId) {
        return queryService.findNextPatientAppointment(patientId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Майбутніх записів пацієнта не знайдено"));
    }
}