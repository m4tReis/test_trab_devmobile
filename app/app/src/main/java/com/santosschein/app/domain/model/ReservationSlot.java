package com.santosschein.app.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalTime;

@Getter
@AllArgsConstructor
public final class ReservationSlot {

    private final String id;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final BigDecimal amount;
}
