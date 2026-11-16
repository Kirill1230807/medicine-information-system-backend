package com.bank.userservice.client.port;

import java.util.UUID;

public interface DoctorAppointmentStatsPort {
    long getActiveAppointmentsCount(UUID doctorId);
}