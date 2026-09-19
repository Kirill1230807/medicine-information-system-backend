package com.bank.medicineinformationsystembackend.dto.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserCreateDTO {
    private String email;
    private String role;
    private String externalId;
}