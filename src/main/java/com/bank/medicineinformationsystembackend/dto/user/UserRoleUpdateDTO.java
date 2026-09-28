package com.bank.medicineinformationsystembackend.dto.user;

/**
 * Запит адміністратора на зміну ролі користувача. Допустимі значення: "DOCTOR" або "PATIENT".
 */
public record UserRoleUpdateDTO(String role) {
}
