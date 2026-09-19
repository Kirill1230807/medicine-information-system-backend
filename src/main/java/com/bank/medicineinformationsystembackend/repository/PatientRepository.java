package com.bank.medicineinformationsystembackend.repository;

import com.bank.medicineinformationsystembackend.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PatientRepository extends JpaRepository<Patient, UUID> {
    // Пошук профілю пацієнта за його прив'язаним акаунтом користувача
    Optional<Patient> findByUserId(UUID userId);
}