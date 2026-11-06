package com.santosschein.app.domain.repository;

import com.santosschein.app.core.util.Result;
import com.santosschein.app.domain.model.Reservation;
import com.santosschein.app.domain.model.ReservationSlot;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository {

    Result<Reservation> getNextReservation();

    Result<List<LocalDate>> getAvailableDays(int year, int month);

    Result<List<ReservationSlot>> getAvailableSlots(LocalDate date);

    Result<Reservation> createReservation(LocalDate date, String slotId);

    Result<List<Reservation>> getUpcomingReservations();

    Result<List<Reservation>> getPastReservations();

    Result<Void> cancelReservation(String reservationId);
}
