package br.com.santosschein.complexo.interfaces.dto.response;

import br.com.santosschein.complexo.domain.model.ReservationStatus;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record ReservationResponse(
        String id,
        String fieldName,
        LocalDate date,

        @JsonFormat(pattern = "HH:mm")
        LocalTime startTime,

        @JsonFormat(pattern = "HH:mm")
        LocalTime endTime,

        BigDecimal price,
        ReservationStatus status
) {
}
