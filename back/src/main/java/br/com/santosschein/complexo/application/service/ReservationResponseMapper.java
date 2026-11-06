package br.com.santosschein.complexo.application.service;

import br.com.santosschein.complexo.domain.model.Reservation;
import br.com.santosschein.complexo.interfaces.dto.response.ReservationResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ReservationResponseMapper {

    public ReservationResponse toResponse(Reservation reservation) {
        return toResponse(reservation, null);
    }

    /**
     * @param now quando informado, reservas ativas cujo horario ja terminou saem como COMPLETED.
     */
    public ReservationResponse toResponse(Reservation reservation, LocalDateTime now) {
        return new ReservationResponse(
                "res_" + reservation.id(),
                reservation.field().name(),
                reservation.reservationDate(),
                reservation.slot().startTime(),
                reservation.slot().endTime(),
                reservation.chargedPrice(),
                reservation.statusAt(now)
        );
    }
}
