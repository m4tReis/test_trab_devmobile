package com.santosschein.app.data.remote.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public final class ReservationSlotDto {

    private String id;
    private String startTime;
    private String endTime;
    private BigDecimal price;
}
