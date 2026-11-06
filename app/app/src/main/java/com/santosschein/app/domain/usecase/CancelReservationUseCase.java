package com.santosschein.app.domain.usecase;

import com.santosschein.app.core.util.Result;
import com.santosschein.app.domain.repository.ReservationRepository;

public final class CancelReservationUseCase {

    private final ReservationRepository reservationRepository;

    public CancelReservationUseCase(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public Result<Void> execute(String reservationId) {
        if (ValidationUtils.isBlank(reservationId)) {
            return ValidationUtils.validationError("Informe a reserva.");
        }
        return reservationRepository.cancelReservation(reservationId.trim());
    }
}
