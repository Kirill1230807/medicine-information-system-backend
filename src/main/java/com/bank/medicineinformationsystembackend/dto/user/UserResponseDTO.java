package com.bank.medicineinformationsystembackend.dto.user;

import lombok.Getter;
import lombok.Setter;
import java.util.UUID;

@Getter
@Setter
public class UserResponseDTO {
    private UUID id;
    private String email;
    private String role;
    private String externalId;
}