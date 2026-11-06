package com.santosschein.app.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@AllArgsConstructor
public final class Reservation {

    private final String id;
    private final String fieldName;
    private final LocalDate date;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final BigDecimal amount;
    private final ReservationStatus status;
}
