package com.bank.userservice.controller;

import com.bank.userservice.client.port.AppointmentCountPort;
import com.bank.userservice.repository.DoctorRepository;
import com.bank.userservice.repository.PatientRepository;
import com.bank.userservice.repository.UserRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final AppointmentCountPort appointmentCountPort;

    public AdminController(UserRepository userRepository,
                           DoctorRepository doctorRepository,
                           PatientRepository patientRepository,
                           AppointmentCountPort appointmentCountPort) {
        this.userRepository = userRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.appointmentCountPort = appointmentCountPort;
    }

    @GetMapping("/stats")
    public Map<String, Long> getStats() {
        Map<String, Long> stats = new LinkedHashMap<>();
        stats.put("users", userRepository.count());
        stats.put("doctors", doctorRepository.count());
        stats.put("patients", patientRepository.count());
        stats.put("appointments", appointmentCountPort.countAppointments());
        return stats;
    }
}