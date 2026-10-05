package com.bank.medicineinformationsystembackend.service;

import com.bank.medicineinformationsystembackend.entity.Patient;
import com.bank.medicineinformationsystembackend.repository.PatientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class PatientService {
    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @Transactional(readOnly = true)
    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Patient getPatientById(UUID id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Пацієнта з ID " + id + " не знайдено"));
    }

    //ім'я та прізвище пацієнта не вводяться вручну: вони копіюються з акаунта на момент створення профілю.
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
        patientRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Patient getPatientByUserId(UUID userId) {
        return patientRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "У цього користувача ще немає профілю пацієнта"));
    }
}