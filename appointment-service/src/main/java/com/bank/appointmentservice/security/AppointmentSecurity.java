package com.bank.appointmentservice.security;

import com.bank.appointmentservice.client.UserServiceClient;
import com.bank.appointmentservice.repository.AppointmentRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Лікар може редагувати лише свої записи, пацієнт бачить лише свої. */
@Component("appointmentSecurity")
public class AppointmentSecurity {

    private final AppointmentRepository appointmentRepository;
    private final UserServiceClient userServiceClient;

    public AppointmentSecurity(AppointmentRepository appointmentRepository,
                               UserServiceClient userServiceClient) {
        this.appointmentRepository = appointmentRepository;
        this.userServiceClient = userServiceClient;
    }

    public boolean isOwnDoctor(UUID appointmentId, Authentication authentication) {
        if (authentication == null || appointmentId == null) {
            return false;
        }
        return appointmentRepository.findById(appointmentId)
                .map(a -> userServiceClient.currentDoctorId().map(a.getDoctorId()::equals).orElse(false))
                .orElse(false);
    }

    public boolean isOwnPatient(UUID appointmentId, Authentication authentication) {
        if (authentication == null || appointmentId == null) {
            return false;
        }
        return appointmentRepository.findById(appointmentId)
                .map(a -> userServiceClient.currentPatientId().map(a.getPatientId()::equals).orElse(false))
                .orElse(false);
    }
}