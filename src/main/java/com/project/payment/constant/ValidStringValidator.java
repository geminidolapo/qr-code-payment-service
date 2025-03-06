package com.project.payment.constant;

import com.project.payment.util.StringUtil;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod(value = StringUtil.class)
public class ValidStringValidator implements ConstraintValidator<ValidPassword, String> {

//    private static final String VALIDATION_REGEX = "^(?=.*[A-Za-z])(?=(?:.*[\\d\\W]){0,2})[A-Za-z\\d\\W]+$";
    private static final String VALIDATION_REGEX = "^[A-Za-z]*([\\d\\W]{0,2})[A-Za-z]*$";

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value.hasValue() && value.matches(VALIDATION_REGEX);
    }
}
