package com.bank.userservice.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Component
public class AppointmentServiceClient {

    private final RestClient restClient;

    public AppointmentServiceClient(@Value("${services.appointment.url}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestInterceptor(new BearerTokenInterceptor())
                .build();
    }

    public void deleteByDoctor(UUID doctorId) {
        delete("/api/appointments/doctor/{id}", doctorId);
    }

    public void deleteByPatient(UUID patientId) {
        delete("/api/appointments/patient/{id}", patientId);
    }

    public long countAppointments() {
        try {
            Long count = restClient.get().uri("/api/appointments/count").retrieve().body(Long.class);
            return count == null ? 0 : count;
        } catch (RestClientException e) {
            throw unavailable(e);
        }
    }

    private void delete(String path, UUID id) {
        try {
            restClient.delete().uri(path, id).retrieve().toBodilessEntity();
        } catch (RestClientException e) {
            throw unavailable(e);
        }
    }

    private ResponseStatusException unavailable(RestClientException e) {
        return new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                "appointment-service недоступний: " + e.getMessage(), e);
    }
}