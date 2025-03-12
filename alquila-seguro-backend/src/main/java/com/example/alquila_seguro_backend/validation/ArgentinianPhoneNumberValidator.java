package com.example.alquila_seguro_backend.validation;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ArgentinianPhoneNumberValidator implements ConstraintValidator<ArgentinianPhoneNumber, String> {

    @Override
    public boolean isValid(String phoneNumber, ConstraintValidatorContext context) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return true; // Permitir valores nulos o vacíos, si es necesario
        }

        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        try {
            Phonenumber.PhoneNumber number = phoneUtil.parse(phoneNumber, "AR"); // "AR" para Argentina
            return phoneUtil.isValidNumber(number);
        } catch (NumberParseException e) {
            return false; // El número no se pudo analizar
        }
    }
}
