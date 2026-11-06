package com.santosschein.app.domain.usecase;

import com.santosschein.app.core.util.Result;
import com.santosschein.app.domain.model.ReservationSlot;
import com.santosschein.app.domain.repository.ReservationRepository;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public final class GetAvailableSlotsUseCase {

    private final ReservationRepository reservationRepository;
    private final Clock clock;

    public GetAvailableSlotsUseCase(ReservationRepository reservationRepository) {
        this(reservationRepository, Clock.systemDefaultZone());
    }

    public GetAvailableSlotsUseCase(ReservationRepository reservationRepository, Clock clock) {
        this.reservationRepository = reservationRepository;
        this.clock = clock;
    }

    public Result<List<ReservationSlot>> execute(LocalDate date) {
        if (date == null) {
            return ValidationUtils.validationError("Informe a data da reserva.");
        }
        if (date.isBefore(LocalDate.now(clock))) {
            return ValidationUtils.validationError("Nao e permitido consultar datas passadas.");
        }
        Result<List<ReservationSlot>> result = reservationRepository.getAvailableSlots(date);
        if (!result.isSuccess() || !date.isEqual(LocalDate.now(clock)) || result.getData() == null) {
            return result;
        }

        LocalTime now = LocalTime.now(clock);
        List<ReservationSlot> futureSlots = new ArrayList<>();
        for (ReservationSlot slot : result.getData()) {
            if (slot.getStartTime().isAfter(now)) {
                futureSlots.add(slot);
            }
        }
        return Result.success(futureSlots);
    }
}
