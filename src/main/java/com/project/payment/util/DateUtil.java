package com.project.payment.util;

import com.project.payment.exception.GenericException;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Slf4j
@UtilityClass
public class DateUtil {
    /**
     * Parses a date string into a LocalDateTime object.
     */
    public LocalDateTime parseDate(String dateStr) {
        if (StringUtil.hasValue(dateStr)) {
            try {
                return LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            } catch (DateTimeParseException e) {
                log.info("Invalid date format provided: {}", dateStr);
                throw new GenericException("Invalid date format");
            }
        }
        return null;
    }
}
