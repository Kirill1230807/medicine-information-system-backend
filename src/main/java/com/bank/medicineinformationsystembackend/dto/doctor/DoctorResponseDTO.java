package com.bank.medicineinformationsystembackend.dto.doctor;

import lombok.Getter;
import lombok.Setter;
import java.util.UUID;

@Getter
@Setter
public class DoctorResponseDTO {
    private UUID id;
    private UUID userId;
    private String firstName;
    private String lastName;
    private String specialization;
    private String cabinetNumber;
}