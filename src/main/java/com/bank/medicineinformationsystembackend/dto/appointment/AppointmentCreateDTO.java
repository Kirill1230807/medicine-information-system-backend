package com.bank.medicineinformationsystembackend.dto.appointment;

import com.bank.medicineinformationsystembackend.validation.annotation.ValidAppointmentTime;
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