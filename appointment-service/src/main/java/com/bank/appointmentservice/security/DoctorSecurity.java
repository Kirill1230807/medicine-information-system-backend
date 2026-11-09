package com.bank.appointmentservice.security;

import com.bank.appointmentservice.client.UserServiceClient;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("doctorSecurity")
public class DoctorSecurity {

    private final UserServiceClient userServiceClient;

    public DoctorSecurity(UserServiceClient userServiceClient) {
        this.userServiceClient = userServiceClient;
    }

    public boolean isSelf(UUID doctorId, Authentication authentication) {
        if (authentication == null || doctorId == null) {
            return false;
        }
        return userServiceClient.currentDoctorId().map(doctorId::equals).orElse(false);
    }
}