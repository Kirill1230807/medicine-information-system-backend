package com.bank.userservice.client.http;

import com.bank.userservice.client.port.PatientNextAppointmentPort.NextAppointmentInfo;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.UUID;

@HttpExchange("/api/appointments")
public interface AppointmentHttpExchangeClient {

    record DoctorStatsResponse(UUID doctorId, long activeAppointmentsCount) {}

    @GetExchange("/count")
    Long countAppointments();

    @GetExchange("/doctor/{doctorId}/stats")
    DoctorStatsResponse getDoctorStats(@PathVariable("doctorId") UUID doctorId);

    @GetExchange("/patient/{patientId}/next")
    NextAppointmentInfo getNextPatientAppointment(@PathVariable("patientId") UUID patientId);
}