package com.bank.medicineinformationsystembackend.security;

import com.bank.medicineinformationsystembackend.repository.AppointmentRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Перевірка "цей запис на прийом стосується поточного лікаря/пацієнта".
 * Лікар може редагувати лише свої записи (де він призначений лікарем),
 * а не записи інших лікарів.
 */
@Component("appointmentSecurity")
public class AppointmentSecurity {

    private final AppointmentRepository appointmentRepository;

    public AppointmentSecurity(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    public boolean isOwnDoctor(UUID appointmentId, Authentication authentication) {
        if (authentication == null || appointmentId == null) {
            return false;
        }
        return appointmentRepository.findById(appointmentId)
                .map(a -> a.getDoctor() != null && a.getDoctor().getUser() != null
                        && authentication.getName().equals(a.getDoctor().getUser().getExternalId()))
                .orElse(false);
    }

    public boolean isOwnPatient(UUID appointmentId, Authentication authentication) {
        if (authentication == null || appointmentId == null) {
            return false;
        }
        return appointmentRepository.findById(appointmentId)
                .map(a -> a.getPatient() != null && a.getPatient().getUser() != null
                        && authentication.getName().equals(a.getPatient().getUser().getExternalId()))
                .orElse(false);
    }
}