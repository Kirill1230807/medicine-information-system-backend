package com.bank.medicineinformationsystembackend.validation.annotation;

import java.lang.annotation.*;

// Кастомна анотація-резолвер аргументів MVC
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuthenticatedUser {
}
