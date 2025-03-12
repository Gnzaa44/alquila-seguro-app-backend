package com.example.alquila_seguro_backend.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ArgentinianPhoneNumberValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ArgentinianPhoneNumber {

    String message() default "El número de teléfono debe ser un número válido de Argentina.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
