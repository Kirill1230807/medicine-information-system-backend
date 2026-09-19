package com.bank.medicineinformationsystembackend.service;

import com.bank.medicineinformationsystembackend.entity.Patient;
import com.bank.medicineinformationsystembackend.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public Patient createPatient(Patient patient) {
        if (patient.getUser() != null && patient.getUser().getId() != null) {
            boolean exists = patientRepository.findByUserId(patient.getUser().getId()).isPresent();
            if (exists) {
                throw new RuntimeException("Профіль пацієнта для цього користувача вже існує");
            }
        }
        return patientRepository.save(patient);
    }

    @Transactional
    public Patient updatePatient(UUID id, Patient updatedData) {
        Patient existingPatient = getPatientById(id);

        existingPatient.setFirstName(updatedData.getFirstName());
        existingPatient.setLastName(updatedData.getLastName());
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
                .orElseThrow(() -> new RuntimeException("Профіль пацієнта для користувача з ID " + userId + " не знайдено"));
    }
}
