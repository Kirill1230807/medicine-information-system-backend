package com.bank.appointmentservice.security;

import com.bank.appointmentservice.client.UserServiceClient;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("patientSecurity")
public class PatientSecurity {

    private final UserServiceClient userServiceClient;

    public PatientSecurity(UserServiceClient userServiceClient) {
        this.userServiceClient = userServiceClient;
    }

    public boolean isSelf(UUID patientId, Authentication authentication) {
        if (authentication == null || patientId == null) {
            return false;
        }
        return userServiceClient.currentPatientId().map(patientId::equals).orElse(false);
    }
}