package com.bank.medicineinformationsystembackend.security;

import com.bank.medicineinformationsystembackend.repository.PatientRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Перевірка "це профіль пацієнта, що належить поточному користувачу".
 * Використовується в @PreAuthorize, щоб пацієнт бачив і змінював лише власні дані.
 */
@Component("patientSecurity")
public class PatientSecurity {

    private final PatientRepository patientRepository;

    public PatientSecurity(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public boolean isSelf(UUID patientId, Authentication authentication) {
        if (authentication == null || patientId == null) {
            return false;
        }
        return patientRepository.findById(patientId)
                .map(patient -> patient.getUser() != null
                        && authentication.getName().equals(patient.getUser().getExternalId()))
                .orElse(false);
    }
}