package com.bank.medicineinformationsystembackend.mapper;

import com.bank.medicineinformationsystembackend.dto.appointment.AppointmentCreateDTO;
import com.bank.medicineinformationsystembackend.dto.appointment.AppointmentResponseDTO;
import com.bank.medicineinformationsystembackend.entity.Appointment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AppointmentMapper {

    @Mapping(source = "doctor.id", target = "doctorId")
    @Mapping(source = "patient.id", target = "patientId")
    AppointmentResponseDTO toDto(Appointment appointment);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "doctor", ignore = true)
    @Mapping(target = "patient", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Appointment toEntity(AppointmentCreateDTO dto);
}
