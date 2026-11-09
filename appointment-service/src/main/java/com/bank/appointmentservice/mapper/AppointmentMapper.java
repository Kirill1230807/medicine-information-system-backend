package com.bank.appointmentservice.mapper;

import com.bank.appointmentservice.dto.appointment.AppointmentCreateDTO;
import com.bank.appointmentservice.dto.appointment.AppointmentResponseDTO;
import com.bank.appointmentservice.entity.Appointment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AppointmentMapper {

    AppointmentResponseDTO toDto(Appointment appointment);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Appointment toEntity(AppointmentCreateDTO dto);
}