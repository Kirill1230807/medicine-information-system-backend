package com.bank.appointmentservice.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

@Component
public class UserServiceClient {

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ProfileDto(UUID id) {}

    private final RestClient restClient;

    public UserServiceClient(@Value("${services.user.url}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestInterceptor(new BearerTokenInterceptor())
                .build();
    }

    /** Кидає 404, якщо лікаря з таким id немає в user-service. */
    public void requireDoctor(UUID doctorId) {
        requireExists("/api/doctors/{id}", doctorId, "Лікаря з ID " + doctorId + " не знайдено");
    }

    /** Кидає 404, якщо пацієнта з таким id немає в user-service. */
    public void requirePatient(UUID patientId) {
        requireExists("/api/patients/{id}", patientId, "Пацієнта з ID " + patientId + " не знайдено");
    }

    /** id профілю лікаря поточного користувача (порожньо, якщо профілю немає). */
    public Optional<UUID> currentDoctorId() {
        return fetchMyId("/api/doctors/me");
    }

    /** id профілю пацієнта поточного користувача (порожньо, якщо профілю немає). */
    public Optional<UUID> currentPatientId() {
        return fetchMyId("/api/patients/me");
    }

    private void requireExists(String path, UUID id, String notFoundMessage) {
        try {
            restClient.get().uri(path, id).retrieve().toBodilessEntity();
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, notFoundMessage);
        } catch (RestClientException e) {
            throw unavailable(e);
        }
    }

    private Optional<UUID> fetchMyId(String path) {
        try {
            ProfileDto profile = restClient.get().uri(path).retrieve().body(ProfileDto.class);
            return Optional.ofNullable(profile).map(ProfileDto::id);
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        } catch (RestClientException e) {
            throw unavailable(e);
        }
    }

    private ResponseStatusException unavailable(RestClientException e) {
        return new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                "user-service недоступний: " + e.getMessage(), e);
    }
}