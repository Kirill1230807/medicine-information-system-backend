package com.bank.appointmentservice.repository;

import com.bank.appointmentservice.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
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

    // Видалити всі записи лікаря/пацієнта (використовується при видаленні їхнього акаунта)
    void deleteByDoctorId(UUID doctorId);

    void deleteByPatientId(UUID patientId);

    @Modifying
    @Query("UPDATE Appointment a SET a.status = :newStatus "
            + "WHERE a.appointmentDatetime < :threshold AND a.status = 'SCHEDULED'")
    int markOutdatedAppointments(
            @Param("threshold") ZonedDateTime threshold,
            @Param("newStatus") String newStatus);
}