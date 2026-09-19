package com.bank.medicineinformationsystembackend.dto.appointment;

import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
@Setter
public class AppointmentCreateDTO {
    private UUID doctorId;
    private UUID patientId;
    private ZonedDateTime appointmentDatetime;
    private String notes;
}