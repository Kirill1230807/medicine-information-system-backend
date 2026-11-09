package com.bank.userservice.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class RetryOnOptimisticLockAspect {

    @Around("@annotation(com.bank.userservice.aspect.annotation.RetryOnOptimisticLock)")
    public Object retry(ProceedingJoinPoint pjp) throws Throwable {
        int maxRetries = 3;
        int attempt = 0;
        while (true) {
            try {
                return pjp.proceed();
            } catch (OptimisticLockingFailureException e) {
                attempt++;
                if (attempt >= maxRetries) {
                    log.error("Перевищено кількість спроб транзакції для {}", pjp.getSignature().getName());
                    throw e;
                }
                log.warn("Конфлікт блокування, спроба {} з {}...", attempt, maxRetries);
            }
        }
    }
}