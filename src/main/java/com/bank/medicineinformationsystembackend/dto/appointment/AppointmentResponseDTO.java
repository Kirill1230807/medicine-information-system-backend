package com.bank.medicineinformationsystembackend.dto.appointment;

import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
@Setter
public class AppointmentResponseDTO {
    private UUID id;
    private UUID doctorId;
    private UUID patientId;
    private ZonedDateTime appointmentDatetime;
    private String status;
    private String notes;
}