package com.bank.userservice.mapper;

import com.bank.userservice.dto.user.UserCreateDTO;
import com.bank.userservice.dto.user.UserResponseDTO;
import com.bank.userservice.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponseDTO toDto(User user);

    @Mapping(target = "id", ignore = true)
    User toEntity(UserCreateDTO dto);
}