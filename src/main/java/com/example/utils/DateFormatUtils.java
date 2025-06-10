package com.example.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class DateFormatUtils {
    private static final DateTimeFormatter DEFAULT_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static String format(Long timestamp) {
        // 时间戳 → Instant → LocalDateTime
        Instant instant = Instant.ofEpochMilli(timestamp);
        LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        return DEFAULT_FORMATTER.format(dateTime);
    }

    public static String format(Long timestamp, String pattern) {
        // 时间戳 → Instant → LocalDateTime
        Instant instant = Instant.ofEpochMilli(timestamp);
        LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        return DateTimeFormatter.ofPattern(pattern).format(dateTime);
    }

}
