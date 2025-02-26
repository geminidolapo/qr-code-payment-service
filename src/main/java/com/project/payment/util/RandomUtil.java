package com.project.payment.util;

import lombok.experimental.UtilityClass;
import java.util.Random;

@UtilityClass
public class RandomUtil {
    private static final String PREFIX = "PAYVERDE";

    public String generateUniqueRef() {
        long randInt = new Random().nextLong((9999999999999999L - 1000000000000000L)+1) + 1000000000000000L;
        return PREFIX+randInt;
    }
}
