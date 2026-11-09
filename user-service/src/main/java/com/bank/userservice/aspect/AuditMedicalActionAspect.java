package com.bank.userservice.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class AuditMedicalActionAspect {

    @Before("@annotation(com.bank.userservice.aspect.annotation.AuditMedicalAction)")
    public void auditAction(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        log.info("AUDIT: Ініційовано медичну дію. Метод: {}, Аргументи: {}", methodName, Arrays.toString(args));
    }
}