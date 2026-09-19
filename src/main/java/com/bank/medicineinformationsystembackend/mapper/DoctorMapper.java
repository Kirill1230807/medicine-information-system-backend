package com.bank.medicineinformationsystembackend.mapper;

import com.bank.medicineinformationsystembackend.dto.doctor.*;
import com.bank.medicineinformationsystembackend.entity.Doctor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DoctorMapper {

    @Mapping(source = "user.id", target = "userId")
    DoctorResponseDTO toDto(Doctor doctor);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    Doctor toEntity(DoctorCreateDTO dto);
}