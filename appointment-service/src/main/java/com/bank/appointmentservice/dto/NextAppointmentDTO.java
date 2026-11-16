package com.bank.appointmentservice.dto;

import java.time.Instant;
import java.util.UUID;

public record NextAppointmentDTO(
        UUID appointmentId,
        UUID doctorId,
        Instant appointmentTime,
        String status
) {}