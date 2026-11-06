package br.com.santosschein.complexo.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record Reservation(
        Long id,
        User user,
        SoccerField field,
        ReservationSlot slot,
        LocalDate reservationDate,
        ReservationStatus status,
        LocalDateTime createdAt,
        LocalDateTime canceledAt,
        BigDecimal price
) {

    /**
     * Construtor de conveniencia: usa o preco atual do horario como valor da reserva.
     */
    public Reservation(
            Long id,
            User user,
            SoccerField field,
            ReservationSlot slot,
            LocalDate reservationDate,
            ReservationStatus status,
            LocalDateTime createdAt,
            LocalDateTime canceledAt
    ) {
        this(id, user, field, slot, reservationDate, status, createdAt, canceledAt, slot != null ? slot.price() : null);
    }

    public LocalDateTime startDateTime() {
        return LocalDateTime.of(reservationDate, slot.startTime());
    }

    public LocalDateTime endDateTime() {
        return LocalDateTime.of(reservationDate, slot.endTime());
    }

    /**
     * Situacao exibida ao cliente: uma reserva ativa cujo horario ja terminou aparece como COMPLETED.
     */
    public ReservationStatus statusAt(LocalDateTime now) {
        if (status == ReservationStatus.ACTIVE && now != null && !endDateTime().isAfter(now)) {
            return ReservationStatus.COMPLETED;
        }
        return status;
    }

    /**
     * Valor copiado no momento da confirmacao. Registros antigos sem copia usam o preco do horario.
     */
    public BigDecimal chargedPrice() {
        if (price != null) {
            return price;
        }
        return slot != null ? slot.price() : null;
    }
}
