package com.bank.appointmentservice.service;

import com.bank.appointmentservice.dto.DoctorAppointmentsStatsDTO;
import com.bank.appointmentservice.dto.NextAppointmentDTO;
import com.bank.appointmentservice.entity.Appointment;
import com.bank.appointmentservice.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AppointmentQueryDomainService {

    private static final String STATUS_SCHEDULED = "SCHEDULED";

    private final AppointmentRepository appointmentRepository;

    public long countAll() {
        return appointmentRepository.count();
    }

    public Optional<DoctorAppointmentsStatsDTO> findDoctorStats(UUID doctorId) {
        List<Appointment> doctorAppointments = appointmentRepository.findAll().stream()
                .filter(a -> doctorId.equals(a.getDoctorId()))
                .toList();

        if (doctorAppointments.isEmpty()) {
            return Optional.empty();
        }

        long activeCount = doctorAppointments.stream()
                .filter(a -> STATUS_SCHEDULED.equalsIgnoreCase(a.getStatus()))
                .count();

        return Optional.of(new DoctorAppointmentsStatsDTO(doctorId, activeCount));
    }

    public Optional<NextAppointmentDTO> findNextPatientAppointment(UUID patientId) {
        ZonedDateTime now = ZonedDateTime.now();

        return appointmentRepository.findAll().stream()
                .filter(a -> patientId.equals(a.getPatientId()))
                .filter(a -> STATUS_SCHEDULED.equalsIgnoreCase(a.getStatus()))
                .filter(a -> a.getAppointmentDatetime() != null && a.getAppointmentDatetime().isAfter(now))
                .min(Comparator.comparing(Appointment::getAppointmentDatetime))
                .map(a -> new NextAppointmentDTO(
                        a.getId(),
                        a.getDoctorId(),
                        a.getAppointmentDatetime().toInstant(),
                        a.getStatus()
                ));
    }
}