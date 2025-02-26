package com.project.payment.constant;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidStringValidator implements ConstraintValidator<ValidPassword, String> {

    private static final String VALIDATION_REGEX = "^[A-Za-z]*([\\d\\W]{0,2})[A-Za-z]*$";

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return true;
        }

        return value.matches(VALIDATION_REGEX);
    }
}
