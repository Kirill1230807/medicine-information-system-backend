package com.bank.medicineinformationsystembackend.mapper;

import com.bank.medicineinformationsystembackend.dto.user.*;
import com.bank.medicineinformationsystembackend.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponseDTO toDto(User user);

    @Mapping(target = "id", ignore = true)
    User toEntity(UserCreateDTO dto);
}