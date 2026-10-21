package com.bank.appointmentservice.dto.appointment;

import com.bank.appointmentservice.validation.annotation.ValidAppointmentTime;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
@Setter
public class AppointmentCreateDTO {
    private UUID doctorId;
    private UUID patientId;

    @ValidAppointmentTime
    private ZonedDateTime appointmentDatetime;
    private String status;
    private String notes;
}