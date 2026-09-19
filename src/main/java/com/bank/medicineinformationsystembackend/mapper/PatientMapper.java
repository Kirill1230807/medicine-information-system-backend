package com.bank.medicineinformationsystembackend.mapper;

import com.bank.medicineinformationsystembackend.dto.patient.*;
import com.bank.medicineinformationsystembackend.entity.Patient;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PatientMapper {

    @Mapping(source = "user.id", target = "userId")
    PatientResponseDTO toDto(Patient patient);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    Patient toEntity(PatientCreateDTO dto);
}