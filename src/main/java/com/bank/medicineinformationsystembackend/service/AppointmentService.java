package com.bank.medicineinformationsystembackend.service;

import com.bank.medicineinformationsystembackend.entity.Appointment;
import com.bank.medicineinformationsystembackend.repository.AppointmentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class AppointmentService {

    private static final Set<String> ALLOWED_STATUSES = Set.of("SCHEDULED", "COMPLETED", "CANCELLED");

    private final AppointmentRepository appointmentRepository;

    public AppointmentService(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    @Transactional(readOnly = true)
    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Appointment getAppointmentById(UUID id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Запис з ID " + id + " не знайдено"));
    }

    @Transactional
    public Appointment createAppointment(Appointment appointment) {

        appointment.setStatus("SCHEDULED");

        if (appointment.getAppointmentDatetime() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Не вказано дату і час прийому");
        }
        if (appointment.getAppointmentDatetime().isBefore(ZonedDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Неможливо створити запис на минулий час");
        }

        return appointmentRepository.save(appointment);
    }

    @Transactional
    public Appointment updateAppointment(UUID id, Appointment updatedData) {
        Appointment existingAppointment = getAppointmentById(id);

        if (updatedData.getAppointmentDatetime() != null) {
            if (updatedData.getAppointmentDatetime().isBefore(ZonedDateTime.now())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Неможливо перенести запис на минулий час");
            }
            existingAppointment.setAppointmentDatetime(updatedData.getAppointmentDatetime());
        }
        existingAppointment.setNotes(updatedData.getNotes());

        if (updatedData.getStatus() != null) {
            if (!ALLOWED_STATUSES.contains(updatedData.getStatus())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Допустимі статуси: SCHEDULED, COMPLETED, CANCELLED");
            }
            existingAppointment.setStatus(updatedData.getStatus());
        }

        return appointmentRepository.save(existingAppointment);
    }

    @Transactional
    public void deleteAppointment(UUID id) {
        if (!appointmentRepository.existsById(id)) {
            throw new RuntimeException("Запис з ID " + id + " не знайдено");
        }
        appointmentRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Appointment> getAppointmentsByPatient(UUID patientId) {
        return appointmentRepository.findByPatientId(patientId);
    }

    @Transactional(readOnly = true)
    public List<Appointment> getAppointmentsByDoctor(UUID doctorId) {
        return appointmentRepository.findByDoctorId(doctorId);
    }

    @Transactional(readOnly = true)
    public List<Appointment> getDoctorAppointmentsByStatus(UUID doctorId, String status) {
        return appointmentRepository.findByDoctorIdAndStatus(doctorId, status);
    }
}