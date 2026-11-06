package com.santosschein.app.data.remote.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public final class CreateReservationRequestDto {

    private final String date;
    private final String slotId;
}
