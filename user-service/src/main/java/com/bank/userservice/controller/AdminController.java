package com.bank.userservice.controller;

import com.bank.userservice.client.AppointmentServiceClient;
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
    private final AppointmentServiceClient appointmentServiceClient;

    public AdminController(UserRepository userRepository,
                           DoctorRepository doctorRepository,
                           PatientRepository patientRepository,
                           AppointmentServiceClient appointmentServiceClient) {
        this.userRepository = userRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.appointmentServiceClient = appointmentServiceClient;
    }

    @GetMapping("/stats")
    public Map<String, Long> getStats() {
        Map<String, Long> stats = new LinkedHashMap<>();
        stats.put("users", userRepository.count());
        stats.put("doctors", doctorRepository.count());
        stats.put("patients", patientRepository.count());
        stats.put("appointments", appointmentServiceClient.countAppointments());
        return stats;
    }
}