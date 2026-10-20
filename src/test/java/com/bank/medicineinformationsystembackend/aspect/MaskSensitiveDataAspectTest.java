package com.bank.medicineinformationsystembackend.aspect;

import com.bank.medicineinformationsystembackend.entity.Patient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MaskSensitiveDataAspectTest {

    private MaskSensitiveDataAspect aspect;

    @BeforeEach
    void setUp() {
        // GIVEN: клас аспекту проходить тестування без контексту Spring
        aspect = new MaskSensitiveDataAspect();
    }

    @Test
    void shouldMaskPhoneNumberWhenPatientReturned() {
        // GIVEN
        Patient patient = new Patient();
        patient.setPhoneNumber("0991234567");

        // WHEN
        aspect.maskData(patient);

        // THEN: перевіряємо фактичну логіку прийняття рішень усередині advice
        assertEquals("***-***-4567", patient.getPhoneNumber());
    }

    @Test
    void shouldNotMaskWhenPhoneIsTooShort() {
        // GIVEN
        Patient patient = new Patient();
        patient.setPhoneNumber("123");

        // WHEN
        aspect.maskData(patient);

        // THEN: телефон не змінився, бо менше 4 символів
        assertEquals("123", patient.getPhoneNumber());
    }

    @Test
    void shouldNotFailWhenPhoneIsNull() {
        // GIVEN
        Patient patient = new Patient();
        patient.setPhoneNumber(null);

        // WHEN
        aspect.maskData(patient);

        // THEN
        assertNull(patient.getPhoneNumber());
    }

    @Test
    void shouldDoNothingWhenNotPatientInstance() {
        // GIVEN
        String notPatient = "Some String Result";

        // WHEN
        aspect.maskData(notPatient);

        // THEN: помилки немає, значення не змінене
        assertEquals("Some String Result", notPatient);
    }
}