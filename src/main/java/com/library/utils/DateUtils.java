package com.library.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Optional;

/**
 * Central place for formatting/parsing dates so every view uses the same pattern.
 */
public final class DateUtils {

    private static final DateTimeFormatter DISPLAY_FORMATTER =
            DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH);

    private DateUtils() {
    }

    /**
     * Formats the provided {@link LocalDate} for display or returns an em dash if null.
     */
    public static String format(LocalDate date) {
        return date == null ? "—" : DISPLAY_FORMATTER.format(date);
    }

    /**
     * Attempts to parse a string using the shared display formatter.
     */
    public static Optional<LocalDate> parse(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(LocalDate.parse(value, DISPLAY_FORMATTER));
        } catch (DateTimeParseException ex) {
            return Optional.empty();
        }
    }
}

