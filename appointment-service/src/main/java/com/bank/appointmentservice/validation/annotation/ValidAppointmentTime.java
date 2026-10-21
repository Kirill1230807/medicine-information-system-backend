package com.bank.appointmentservice.validation.annotation;

import com.bank.appointmentservice.validation.resolver.AppointmentTimeValidator;
import jakarta.validation.Constraint;

import java.lang.annotation.*;

// Bean Validation Constraint
@Documented
@Constraint(validatedBy = { AppointmentTimeValidator.class })
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidAppointmentTime {
    String message() default "Час прийому має бути в робочі дні (Пн-Пт) з 08:00 до 18:00";

    Class[] groups() default {};

    Class[] payload() default {};
}