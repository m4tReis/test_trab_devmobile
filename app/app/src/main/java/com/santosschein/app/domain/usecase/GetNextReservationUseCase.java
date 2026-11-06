package com.santosschein.app.domain.usecase;

import com.santosschein.app.core.util.Result;
import com.santosschein.app.domain.model.Reservation;
import com.santosschein.app.domain.repository.ReservationRepository;

public final class GetNextReservationUseCase {

    private final ReservationRepository reservationRepository;

    public GetNextReservationUseCase(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public Result<Reservation> execute() {
        return reservationRepository.getNextReservation();
    }
}
