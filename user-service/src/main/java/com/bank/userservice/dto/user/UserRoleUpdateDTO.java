package com.bank.userservice.dto.user;

/**
 * Запит адміністратора на зміну ролі користувача. Допустимі значення: "DOCTOR" або "PATIENT".
 */
public record UserRoleUpdateDTO(String role) {
}
