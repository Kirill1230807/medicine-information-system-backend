package com.bank.medicineinformationsystembackend.validation;

import com.bank.medicineinformationsystembackend.validation.annotation.ValidAppointmentTime;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.DayOfWeek;
import java.time.ZonedDateTime;

public class AppointmentTimeValidator implements ConstraintValidator<ValidAppointmentTime, ZonedDateTime> {

    @Override
    public boolean isValid(ZonedDateTime value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        DayOfWeek day = value.getDayOfWeek();
        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
            return false;
        }

        int hour = value.getHour();
        return hour >= 8 && hour < 18;
    }
}