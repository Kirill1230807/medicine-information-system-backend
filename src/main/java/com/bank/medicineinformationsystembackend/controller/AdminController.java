package com.bank.medicineinformationsystembackend.controller;

import com.bank.medicineinformationsystembackend.repository.AppointmentRepository;
import com.bank.medicineinformationsystembackend.repository.DoctorRepository;
import com.bank.medicineinformationsystembackend.repository.PatientRepository;
import com.bank.medicineinformationsystembackend.repository.UserRepository;
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
    private final AppointmentRepository appointmentRepository;

    public AdminController(UserRepository userRepository,
                           DoctorRepository doctorRepository,
                           PatientRepository patientRepository,
                           AppointmentRepository appointmentRepository) {
        this.userRepository = userRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
    }

    @GetMapping("/stats")
    public Map<String, Long> getStats() {
        Map<String, Long> stats = new LinkedHashMap<>();
        stats.put("users", userRepository.count());
        stats.put("doctors", doctorRepository.count());
        stats.put("patients", patientRepository.count());
        stats.put("appointments", appointmentRepository.count());
        return stats;
    }
}