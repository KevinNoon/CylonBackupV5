package com.optimised.cylonbackup.tools;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

public class Conversions {
    private static final Pattern isInteger = Pattern.compile("[+-]?\\d+");

    public static int tryParseInt(String value) {
        if (value == null || !isInteger.matcher(value).matches()) {
            return 0;
        }
        try {
            return Integer.parseInt(value);
        } catch(NumberFormatException nfe) {
            return 0;
        }
    }

    public static LocalDateTime tryParseDateTime(String value){

        if (value.length() != 19){
            return null;
        } else {
            int day = tryParseInt(value.substring(0, 2));
            int month = tryParseInt(value.substring(3, 5));
            int year = tryParseInt(value.substring(6, 10));
            int hour = tryParseInt(value.substring(11, 13));
            int min = tryParseInt(value.substring(14, 16));
            int sec = tryParseInt(value.substring(17, 19));
            if (day == 0 || month == 0 || year == 0) return null;
            return LocalDateTime.of(year,month,day,hour,min,sec);
        }
    }
}
