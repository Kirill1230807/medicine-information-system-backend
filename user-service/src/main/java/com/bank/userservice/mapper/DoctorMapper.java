package com.bank.userservice.mapper;

import com.bank.userservice.dto.doctor.DoctorCreateDTO;
import com.bank.userservice.dto.doctor.DoctorResponseDTO;
import com.bank.userservice.entity.Doctor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DoctorMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.firstName", target = "firstName")
    @Mapping(source = "user.lastName", target = "lastName")
    DoctorResponseDTO toDto(Doctor doctor);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "firstName", ignore = true)
    @Mapping(target = "lastName", ignore = true)
    Doctor toEntity(DoctorCreateDTO dto);
}