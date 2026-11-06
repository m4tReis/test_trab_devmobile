package com.santosschein.app.domain.usecase;

import com.santosschein.app.core.util.Result;
import com.santosschein.app.domain.repository.ReservationRepository;

import java.time.LocalDate;
import java.util.List;

public final class GetAvailableDaysUseCase {

    private final ReservationRepository reservationRepository;

    public GetAvailableDaysUseCase(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public Result<List<LocalDate>> execute(int year, int month) {
        if (year < 1) {
            return ValidationUtils.validationError("Informe um ano valido.");
        }
        if (month < 1 || month > 12) {
            return ValidationUtils.validationError("Informe um mes valido.");
        }
        return reservationRepository.getAvailableDays(year, month);
    }
}
