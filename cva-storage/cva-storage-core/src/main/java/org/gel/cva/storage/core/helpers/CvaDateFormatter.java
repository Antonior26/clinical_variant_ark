package org.gel.cva.storage.core.helpers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Created by priesgo on 24/01/17.
 */
public class CvaDateFormatter {

    public static String getCurrentFormattedDate() {
        return CvaDateFormatter.getFormattedDate(LocalDateTime.now());
    }

    public static String getFormattedDate(LocalDateTime date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("YYYYMMddHHmm");
        return date.format(formatter);
    }
}
