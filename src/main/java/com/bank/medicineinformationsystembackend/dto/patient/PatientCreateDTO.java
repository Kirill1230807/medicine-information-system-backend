package com.bank.medicineinformationsystembackend.dto.patient;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class PatientCreateDTO {
    private UUID userId;

    @Pattern(regexp = "^[0-9+()\\-\\s]*$", message = "Номер телефону може містити лише цифри, пробіли, дужки, + та -")
    private String phoneNumber;

    @PastOrPresent(message = "Дата народження не може бути в майбутньому")
    private LocalDate dateOfBirth;
}