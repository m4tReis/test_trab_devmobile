package com.santosschein.app.data.mock;

import com.santosschein.app.core.error.AppError;
import com.santosschein.app.core.error.ErrorType;
import com.santosschein.app.core.util.Result;
import com.santosschein.app.domain.model.Reservation;
import com.santosschein.app.domain.model.ReservationSlot;
import com.santosschein.app.domain.model.ReservationStatus;
import com.santosschein.app.domain.repository.ReservationRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class MockReservationRepository implements ReservationRepository {

    private final MockDataStore dataStore;

    public MockReservationRepository() {
        this(new MockDataStore());
    }

    public MockReservationRepository(MockDataStore dataStore) {
        this.dataStore = dataStore;
    }

    @Override
    public Result<Reservation> getNextReservation() {
        if (dataStore.getUpcomingReservations().isEmpty()) {
            return Result.success(null);
        }
        return Result.success(dataStore.getUpcomingReservations().get(0));
    }

    @Override
    public Result<List<LocalDate>> getAvailableDays(int year, int month) {
        List<LocalDate> days = new ArrayList<>();
        for (LocalDate date : dataStore.getSlotsByDate().keySet()) {
            if (date.getYear() == year && date.getMonthValue() == month
                    && !dataStore.getSlotsByDate().get(date).isEmpty()) {
                days.add(date);
            }
        }
        Collections.sort(days);
        return Result.success(days);
    }

    @Override
    public Result<List<ReservationSlot>> getAvailableSlots(LocalDate date) {
        List<ReservationSlot> slots = dataStore.getSlotsByDate().get(date);
        if (slots == null) {
            return Result.success(Collections.emptyList());
        }
        return Result.success(new ArrayList<>(slots));
    }

    @Override
    public Result<Reservation> createReservation(LocalDate date, String slotId) {
        List<ReservationSlot> slots = dataStore.getSlotsByDate().get(date);
        if (slots == null) {
            return unavailableSlotError();
        }

        ReservationSlot selectedSlot = null;
        for (ReservationSlot slot : slots) {
            if (slot.getId().equals(slotId)) {
                selectedSlot = slot;
                break;
            }
        }

        if (selectedSlot == null) {
            return unavailableSlotError();
        }

        slots.remove(selectedSlot);
        Reservation reservation = new Reservation(
                "res_mock_" + (dataStore.getUpcomingReservations().size() + 1),
                "Campo 1",
                date,
                selectedSlot.getStartTime(),
                selectedSlot.getEndTime(),
                selectedSlot.getAmount(),
                ReservationStatus.ACTIVE
        );
        dataStore.getUpcomingReservations().add(reservation);
        return Result.success(reservation);
    }

    @Override
    public Result<List<Reservation>> getUpcomingReservations() {
        return Result.success(new ArrayList<>(dataStore.getUpcomingReservations()));
    }

    @Override
    public Result<List<Reservation>> getPastReservations() {
        return Result.success(new ArrayList<>(dataStore.getPastReservations()));
    }

    @Override
    public Result<Void> cancelReservation(String reservationId) {
        for (int i = 0; i < dataStore.getUpcomingReservations().size(); i++) {
            Reservation reservation = dataStore.getUpcomingReservations().get(i);
            if (reservation.getId().equals(reservationId)) {
                if (!reservation.getDate().isAfter(LocalDate.now().plusDays(1))) {
                    return Result.failure(new AppError(
                            ErrorType.CONFLICT,
                            "Nao e possivel cancelar com menos de 24 horas de antecedencia.",
                            "409",
                            null
                    ));
                }
                dataStore.getUpcomingReservations().set(i, new Reservation(
                        reservation.getId(),
                        reservation.getFieldName(),
                        reservation.getDate(),
                        reservation.getStartTime(),
                        reservation.getEndTime(),
                        reservation.getAmount(),
                        ReservationStatus.CANCELLED
                ));
                return Result.success(null);
            }
        }
        return Result.failure(new AppError(ErrorType.NOT_FOUND, "Reserva nao encontrada.", "404", null));
    }

    private <T> Result<T> unavailableSlotError() {
        return Result.failure(new AppError(
                ErrorType.CONFLICT,
                "Esse horario nao esta mais disponivel.",
                "409",
                null
        ));
    }
}
