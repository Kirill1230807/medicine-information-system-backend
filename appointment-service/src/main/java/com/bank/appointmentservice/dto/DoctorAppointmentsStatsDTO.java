package com.bank.appointmentservice.dto;

import java.util.UUID;

public record DoctorAppointmentsStatsDTO(
        UUID doctorId,
        long activeAppointmentsCount
) {}