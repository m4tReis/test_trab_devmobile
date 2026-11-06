package com.santosschein.app.presentation.reservation.create;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public final class CalendarDay {

    private final LocalDate date;
    private final boolean inVisibleMonth;
    private final boolean available;
    private final boolean selected;
}
