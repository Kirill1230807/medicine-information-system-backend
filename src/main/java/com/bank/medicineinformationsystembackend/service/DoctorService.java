package com.bank.medicineinformationsystembackend.service;

import com.bank.medicineinformationsystembackend.entity.Doctor;
import com.bank.medicineinformationsystembackend.repository.DoctorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public Doctor createDoctor(Doctor doctor) {
        if (doctor.getUser() != null && doctor.getUser().getId() != null) {
            boolean exists = doctorRepository.findByUserId(doctor.getUser().getId()).isPresent();
            if (exists) {
                throw new RuntimeException("Профіль лікаря для цього користувача вже існує");
            }
        }
        return doctorRepository.save(doctor);
    }

    @Transactional
    public Doctor updateDoctor(UUID id, Doctor updatedData) {
        Doctor existingDoctor = getDoctorById(id);

        existingDoctor.setFirstName(updatedData.getFirstName());
        existingDoctor.setLastName(updatedData.getLastName());
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
                .orElseThrow(() -> new RuntimeException("Профіль лікаря для користувача з ID " + userId + " не знайдено"));
    }

    @Transactional(readOnly = true)
    public List<Doctor> getDoctorsBySpecialization(String specialization) {
        return doctorRepository.findBySpecialization(specialization);
    }
}
