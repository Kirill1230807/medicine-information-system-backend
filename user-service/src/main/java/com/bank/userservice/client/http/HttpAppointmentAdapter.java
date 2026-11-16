package com.bank.userservice.client.http;

import com.bank.userservice.client.port.AppointmentCountPort;
import com.bank.userservice.client.port.DoctorAppointmentStatsPort;
import com.bank.userservice.client.port.PatientNextAppointmentPort;
import com.bank.userservice.exception.RemoteResourceNotFoundException;
import com.bank.userservice.exception.RemoteServiceUnavailableException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

import java.util.UUID;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class HttpAppointmentAdapter implements AppointmentCountPort, DoctorAppointmentStatsPort, PatientNextAppointmentPort {

    private final AppointmentHttpExchangeClient httpClient;

    @Override
    public long countAppointments() {
        Long count = execute(() -> httpClient.countAppointments());
        return count != null ? count : 0L;
    }

    @Override
    public long getActiveAppointmentsCount(UUID doctorId) {
        var response = execute(() -> httpClient.getDoctorStats(doctorId));
        return response != null ? response.activeAppointmentsCount() : 0L;
    }

    @Override
    public NextAppointmentInfo getNextAppointment(UUID patientId) {
        return execute(() -> httpClient.getNextPatientAppointment(patientId));
    }

    private <T> T execute(Supplier<T> call) {
        try {
            return call.get();
        } catch (HttpClientErrorException.NotFound e) {
            throw new RemoteResourceNotFoundException("Ресурс у appointment-service не знайдено (HTTP 404)");
        } catch (RestClientException e) {
            throw new RemoteServiceUnavailableException("appointment-service недоступний по HTTP: " + e.getMessage(), e);
        }
    }
}