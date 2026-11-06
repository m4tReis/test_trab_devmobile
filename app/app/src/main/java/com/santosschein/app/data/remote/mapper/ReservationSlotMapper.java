package com.santosschein.app.data.remote.mapper;

import com.santosschein.app.data.remote.dto.ReservationSlotDto;
import com.santosschein.app.domain.model.ReservationSlot;

import java.time.LocalTime;

public final class ReservationSlotMapper {

    public ReservationSlot toDomain(ReservationSlotDto dto) {
        return new ReservationSlot(
                dto.getId(),
                LocalTime.parse(dto.getStartTime()),
                LocalTime.parse(dto.getEndTime()),
                dto.getPrice()
        );
    }
}
