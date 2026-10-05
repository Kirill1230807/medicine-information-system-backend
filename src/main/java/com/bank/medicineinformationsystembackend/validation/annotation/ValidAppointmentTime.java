package com.bank.medicineinformationsystembackend.validation.annotation;

import com.bank.medicineinformationsystembackend.validation.AppointmentTimeValidator;
import jakarta.validation.Constraint;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = { AppointmentTimeValidator.class })
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidAppointmentTime {
    String message() default "Час прийому має бути в робочі дні (Пн-Пт) з 08:00 до 18:00";

    Class[] groups() default {};

    Class[] payload() default {};
}