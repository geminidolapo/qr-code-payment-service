package com.project.payment.util;

import lombok.experimental.UtilityClass;
import java.util.Random;

@UtilityClass
public class RandomUtil {
    private static final String REFERENCE_PREFIX = "PAYVERDE";
    private static final Long MAX_VALUE = 9999999999999999L;
    private static final Long MIN_VALUE = 1000000000000000L;

    public String generateUniqueRef() {
        long randInt = new Random().nextLong((MAX_VALUE - MIN_VALUE)+1) + MIN_VALUE;
        return REFERENCE_PREFIX+randInt;
    }
}
