package com.bank.medicineinformationsystembackend.service;

import com.bank.medicineinformationsystembackend.entity.Doctor;
import com.bank.medicineinformationsystembackend.repository.DoctorRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class DoctorService {
    private final DoctorRepository doctorRepository;

    public DoctorService(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    @Transactional(readOnly = true)
    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Doctor getDoctorById(UUID id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Лікаря з ID " + id + " не знайдено"));
    }

    /**
     * Ім'я та прізвище лікаря не вводяться вручну: вони копіюються з акаунта
     * (заповнені при реєстрації в Keycloak) на момент створення профілю.
     */
    @Transactional
    public Doctor createDoctor(Doctor doctor) {
        if (doctor.getUser() != null && doctor.getUser().getId() != null) {
            boolean exists = doctorRepository.findByUserId(doctor.getUser().getId()).isPresent();
            if (exists) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Профіль лікаря для цього користувача вже існує");
            }
        }

        String firstName = doctor.getUser() != null ? doctor.getUser().getFirstName() : null;
        String lastName = doctor.getUser() != null ? doctor.getUser().getLastName() : null;
        if (firstName == null || firstName.isBlank() || lastName == null || lastName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "У обраного користувача немає імені та прізвища. Попросіть його увійти в застосунок ще раз.");
        }
        doctor.setFirstName(firstName);
        doctor.setLastName(lastName);

        return doctorRepository.save(doctor);
    }

    @Transactional
    public Doctor updateDoctor(UUID id, Doctor updatedData) {
        Doctor existingDoctor = getDoctorById(id);

        // Ім'я та прізвище лишаються прив'язаними до акаунта і тут не змінюються
        existingDoctor.setSpecialization(updatedData.getSpecialization());
        existingDoctor.setCabinetNumber(updatedData.getCabinetNumber());

        return doctorRepository.save(existingDoctor);
    }

    @Transactional
    public void deleteDoctor(UUID id) {
        if (!doctorRepository.existsById(id)) {
            throw new RuntimeException("Лікаря з ID " + id + " не знайдено");
        }
        doctorRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Doctor getDoctorByUserId(UUID userId) {
        return doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "У цього користувача ще немає профілю лікаря"));
    }

    @Transactional(readOnly = true)
    public List<Doctor> getDoctorsBySpecialization(String specialization) {
        return doctorRepository.findBySpecialization(specialization);
    }
}