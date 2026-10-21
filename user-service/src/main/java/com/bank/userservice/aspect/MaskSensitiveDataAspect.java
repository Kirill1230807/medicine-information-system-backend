package com.bank.userservice.aspect;

import com.bank.userservice.entity.Patient;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.List;

@Aspect
@Component
public class MaskSensitiveDataAspect {

    @AfterReturning(
            pointcut = "@annotation(com.bank.userservice.aspect.annotation.MaskSensitiveData)",
            returning = "result"
    )
    public void maskData(Object result) {
        if (result instanceof Patient patient) {
            maskPatientPhone(patient);
        } else if (result instanceof List<?> list) {
            for (Object item : list) {
                if (item instanceof Patient) {
                    maskPatientPhone((Patient) item);
                }
            }
        }
    }

    private void maskPatientPhone(Patient patient) {
        String phoneNumber = patient.getPhoneNumber();
        if (phoneNumber != null && phoneNumber.length() >= 4) {
            String masked = "***-***-" + phoneNumber.substring(phoneNumber.length() - 4);
            patient.setPhoneNumber(masked);
        }
    }
}