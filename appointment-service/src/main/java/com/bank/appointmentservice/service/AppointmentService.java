package com.bank.appointmentservice.service;

import com.bank.appointmentservice.entity.*;
import com.bank.appointmentservice.repository.AppointmentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class AppointmentService {

    private static final Set<String> ALLOWED_STATUSES = Set.of("SCHEDULED", "COMPLETED", "CANCELLED");

    private final AppointmentRepository appointmentRepository;

    private final MedicalSideEffectService sideEffectService;

    public AppointmentService(AppointmentRepository appointmentRepository, MedicalSideEffectService sideEffectService) {
        this.appointmentRepository = appointmentRepository;
        this.sideEffectService = sideEffectService;
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

        Appointment savedAppointment = appointmentRepository.save(appointment);

        String userEmail = "patient@example.com";

        sideEffectService.sendEmailNotification(userEmail);

        CompletableFuture<String> pdfFuture = sideEffectService.generatePdfReport();
        CompletableFuture<String> extSystemFuture = sideEffectService.notifyExternalSystem();

        String asyncDetails = pdfFuture.thenCombine(extSystemFuture, (pdfResult, extResult) ->
                        String.format("Документ: %s, Інтеграція: %s", pdfResult, extResult))
                .orTimeout(2, TimeUnit.SECONDS)
                .exceptionally(ex -> {
                    log.error("Помилка фонових завдань під час створення запису: {}", ex.getMessage());
                    return "Додаткова інформація тимчасово недоступна";
                })
                .join();

        log.info("Запис створено. Деталі фонових завдань: {}", asyncDetails);

        return savedAppointment;
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

    @Transactional
    public void deleteByDoctorId(UUID doctorId) {
        log.info("Deleting all appointments for deleted doctor: doctorId={}", doctorId);
        appointmentRepository.deleteAllByDoctorId(doctorId);
    }

    @Transactional
    public void deleteByPatientId(UUID patientId) {
        log.info("Deleting all appointments for deleted patient: patientId={}", patientId);
        appointmentRepository.deleteAllByPatientId(patientId);
    }

    @Transactional
    public void updateDoctorCabinet(UUID doctorId, String newCabinet) {
        log.info("Updating cabinet to '{}' for appointments of doctor: doctorId={}", newCabinet, doctorId);
        List<Appointment> appointments = appointmentRepository.findAllByDoctorId(doctorId);

        for (Appointment appointment : appointments) {
            if (!"CANCELLED".equalsIgnoreCase(String.valueOf(appointment.getStatus()))
                    && !"COMPLETED".equalsIgnoreCase(String.valueOf(appointment.getStatus()))) {

                String notePrefix = "Новий кабінет лікаря: " + newCabinet;
                String currentNotes = appointment.getNotes();
                if (currentNotes == null || currentNotes.isBlank()) {
                    appointment.setNotes(notePrefix);
                } else if (!currentNotes.contains(notePrefix)) {
                    appointment.setNotes(currentNotes + " | " + notePrefix);
                }
            }
        }

        appointmentRepository.saveAll(appointments);
    }
}