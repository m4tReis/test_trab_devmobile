package com.santosschein.app.presentation.common;

import com.santosschein.app.domain.model.ReservationStatus;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

public final class PresentationFormatters {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy", new Locale("pt", "BR"));
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm", new Locale("pt", "BR"));

    private PresentationFormatters() {
    }

    public static String date(LocalDate date) {
        return date != null ? DATE_FORMAT.format(date) : "";
    }

    public static String time(LocalTime time) {
        return time != null ? TIME_FORMAT.format(time) : "";
    }

    public static String money(BigDecimal amount) {
        if (amount == null) {
            return "";
        }
        return NumberFormat.getCurrencyInstance(new Locale("pt", "BR")).format(amount);
    }

    public static String status(ReservationStatus status) {
        if (status == null) {
            return "";
        }
        switch (status) {
            case ACTIVE:
                return "Ativa";
            case CANCELLED:
                return "Cancelada";
            case COMPLETED:
                return "Concluída";
            default:
                return status.name();
        }
    }

    public static String cpf(String document) {
        String digits = onlyDigits(document);
        if (digits.length() != 11) {
            return document != null ? document : "";
        }
        return digits.substring(0, 3)
                + "."
                + digits.substring(3, 6)
                + "."
                + digits.substring(6, 9)
                + "-"
                + digits.substring(9);
    }

    public static String onlyDigits(String value) {
        return value == null ? "" : value.replaceAll("\\D", "");
    }

    public static LocalDate parseBrazilianDate(String value) {
        try {
            return LocalDate.parse(value, DATE_FORMAT);
        } catch (DateTimeParseException exception) {
            return null;
        }
    }
}
