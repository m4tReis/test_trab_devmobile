package com.santosschein.app.data.remote.mapper;

import com.santosschein.app.data.remote.dto.ReservationDto;
import com.santosschein.app.domain.model.Reservation;
import com.santosschein.app.domain.model.ReservationStatus;

import java.time.LocalDate;
import java.time.LocalTime;

public final class ReservationMapper {

    public Reservation toDomain(ReservationDto dto) {
        return new Reservation(
                dto.getId(),
                dto.getFieldName(),
                LocalDate.parse(dto.getDate()),
                LocalTime.parse(dto.getStartTime()),
                LocalTime.parse(dto.getEndTime()),
                dto.getPrice(),
                mapStatus(dto.getStatus())
        );
    }

    private ReservationStatus mapStatus(String status) {
        if ("ACTIVE".equalsIgnoreCase(status)) {
            return ReservationStatus.ACTIVE;
        }
        // O back grava "CANCELED"; "CANCELLED" e a grafia usada no relatorio.
        if ("CANCELED".equalsIgnoreCase(status) || "CANCELLED".equalsIgnoreCase(status)) {
            return ReservationStatus.CANCELLED;
        }
        if ("COMPLETED".equalsIgnoreCase(status)) {
            return ReservationStatus.COMPLETED;
        }
        return ReservationStatus.ACTIVE;
    }
}
