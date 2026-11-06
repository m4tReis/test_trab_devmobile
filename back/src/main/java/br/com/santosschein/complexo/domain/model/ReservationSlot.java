package br.com.santosschein.complexo.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record ReservationSlot(
        Long id,
        SoccerField field,
        LocalTime startTime,
        LocalTime endTime,
        BigDecimal price,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
