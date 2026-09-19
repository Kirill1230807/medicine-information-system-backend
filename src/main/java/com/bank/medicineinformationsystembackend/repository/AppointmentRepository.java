package com.bank.medicineinformationsystembackend.repository;

import com.bank.medicineinformationsystembackend.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment,UUID> {
    // Отримати всі записи конкретного пацієнта
    List<Appointment> findByPatientId(UUID patientId);

    // Отримати всі записи до конкретного лікаря
    List<Appointment> findByDoctorId(UUID doctorId);

    // Отримати всі записи конкретного лікаря з певним статусом (наприклад, 'SCHEDULED')
    List<Appointment> findByDoctorIdAndStatus(UUID doctorId, String status);
}