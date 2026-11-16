package com.bank.userservice.client.port;

import java.time.Instant;
import java.util.UUID;

public interface PatientNextAppointmentPort {

    record NextAppointmentInfo(
            UUID appointmentId,
            UUID doctorId,
            Instant appointmentTime,
            String status
    ) {}

    NextAppointmentInfo getNextAppointment(UUID patientId);
}