package com.bank.medicineinformationsystembackend.repository;

import com.bank.medicineinformationsystembackend.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor,UUID> {
    // Пошук профілю лікаря за його прив'язаним акаунтом
    Optional<Doctor> findByUserId(UUID userId);

    // Пошук лікарів за спеціалізацією (наприклад, знайти всіх кардіологів)
    List<Doctor> findBySpecialization(String specialization);
}