package com.santosschein.app.data.remote.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public final class AvailableSlotsResponseDto {

    private List<ReservationSlotDto> slots;
}
