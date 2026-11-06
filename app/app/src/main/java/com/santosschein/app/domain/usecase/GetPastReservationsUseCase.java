package com.santosschein.app.domain.usecase;

import com.santosschein.app.core.util.Result;
import com.santosschein.app.domain.model.Reservation;
import com.santosschein.app.domain.repository.ReservationRepository;

import java.util.List;

public final class GetPastReservationsUseCase {

    private final ReservationRepository reservationRepository;

    public GetPastReservationsUseCase(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public Result<List<Reservation>> execute() {
        return reservationRepository.getPastReservations();
    }
}
