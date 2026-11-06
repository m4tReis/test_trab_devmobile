package com.santosschein.app.domain.usecase;

import com.santosschein.app.core.util.Result;
import com.santosschein.app.domain.model.Reservation;
import com.santosschein.app.domain.model.ReservationSlot;
import com.santosschein.app.domain.repository.ReservationRepository;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;

public final class CreateReservationUseCase {

    private final ReservationRepository reservationRepository;
    private final Clock clock;

    public CreateReservationUseCase(ReservationRepository reservationRepository) {
        this(reservationRepository, Clock.systemDefaultZone());
    }

    public CreateReservationUseCase(ReservationRepository reservationRepository, Clock clock) {
        this.reservationRepository = reservationRepository;
        this.clock = clock;
    }

    public Result<Reservation> execute(LocalDate date, String slotId) {
        if (date == null) {
            return ValidationUtils.validationError("Informe a data da reserva.");
        }
        if (date.isBefore(LocalDate.now(clock))) {
            return ValidationUtils.validationError("Nao e permitido reservar datas passadas.");
        }
        if (ValidationUtils.isBlank(slotId)) {
            return ValidationUtils.validationError("Informe o horario da reserva.");
        }
        return reservationRepository.createReservation(date, slotId.trim());
    }

    public Result<Reservation> execute(LocalDate date, ReservationSlot slot) {
        if (slot == null) {
            return ValidationUtils.validationError("Informe o horario da reserva.");
        }
        if (date != null && date.isEqual(LocalDate.now(clock)) && !slot.getStartTime().isAfter(LocalTime.now(clock))) {
            return ValidationUtils.validationError("Nao e permitido reservar horarios passados.");
        }
        return execute(date, slot.getId());
    }
}
