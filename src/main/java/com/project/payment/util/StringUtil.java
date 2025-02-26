package com.project.payment.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class StringUtil {
    public boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public boolean hasValue(String str) {
        return !isNullOrEmpty(str);
    }
}
