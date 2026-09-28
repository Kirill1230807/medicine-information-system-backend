package com.bank.medicineinformationsystembackend.security;

import com.bank.medicineinformationsystembackend.repository.DoctorRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Перевірка "це профіль лікаря, що належить поточному користувачу".
 * Використовується в @PreAuthorize, щоб лікар міг змінювати лише власний профіль.
 */
@Component("doctorSecurity")
public class DoctorSecurity {

    private final DoctorRepository doctorRepository;

    public DoctorSecurity(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    public boolean isSelf(UUID doctorId, Authentication authentication) {
        if (authentication == null || doctorId == null) {
            return false;
        }
        return doctorRepository.findById(doctorId)
                .map(doctor -> doctor.getUser() != null
                        && authentication.getName().equals(doctor.getUser().getExternalId()))
                .orElse(false);
    }
}