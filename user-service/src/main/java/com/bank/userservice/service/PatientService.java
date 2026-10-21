package com.bank.userservice.service;

import com.bank.userservice.aspect.annotation.AuditMedicalAction;
import com.bank.userservice.aspect.annotation.MaskSensitiveData;
import com.bank.userservice.aspect.annotation.RetryOnOptimisticLock;
import com.bank.userservice.client.AppointmentServiceClient;
import com.bank.userservice.entity.Patient;
import com.bank.userservice.repository.PatientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class PatientService {
    private final PatientRepository patientRepository;
    private final AppointmentServiceClient appointmentServiceClient;

    public PatientService(PatientRepository patientRepository, AppointmentServiceClient appointmentServiceClient) {
        this.patientRepository = patientRepository;
        this.appointmentServiceClient = appointmentServiceClient;
    }

    @MaskSensitiveData
    @Transactional(readOnly = true)
    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    @MaskSensitiveData
    @Transactional(readOnly = true)
    public Patient getPatientById(UUID id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Пацієнта з ID " + id + " не знайдено"));
    }

    @Transactional(readOnly = true)
    public Patient getPatientByIdInternal(UUID id) {
        return this.getPatientById(id); // або просто getPatientById(id)
    }

    //ім'я та прізвище пацієнта не вводяться вручну: вони копіюються з акаунта на момент створення профілю.
    @AuditMedicalAction
    @Transactional
    public Patient createPatient(Patient patient) {
        if (patient.getUser() != null && patient.getUser().getId() != null) {
            boolean exists = patientRepository.findByUserId(patient.getUser().getId()).isPresent();
            if (exists) {
                throw new RuntimeException("Профіль пацієнта для цього користувача вже існує");
            }
        }

        String firstName = patient.getUser() != null ? patient.getUser().getFirstName() : null;
        String lastName = patient.getUser() != null ? patient.getUser().getLastName() : null;
        if (firstName == null || firstName.isBlank() || lastName == null || lastName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "У обраного користувача немає імені та прізвища. Попросіть його увійти в застосунок ще раз.");
        }
        patient.setFirstName(firstName);
        patient.setLastName(lastName);

        return patientRepository.save(patient);
    }

    @RetryOnOptimisticLock
    @Transactional
    public Patient updatePatient(UUID id, Patient updatedData) {
        Patient existingPatient = getPatientById(id);

        existingPatient.setPhoneNumber(updatedData.getPhoneNumber());
        existingPatient.setDateOfBirth(updatedData.getDateOfBirth());

        return patientRepository.save(existingPatient);
    }

    @Transactional
    public void deletePatient(UUID id) {
        if (!patientRepository.existsById(id)) {
            throw new RuntimeException("Пацієнта з ID " + id + " не знайдено");
        }
        appointmentServiceClient.deleteByPatient(id);
        patientRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Patient getPatientByUserId(UUID userId) {
        return patientRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "У цього користувача ще немає профілю пацієнта"));
    }
}