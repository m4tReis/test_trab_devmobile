package br.com.santosschein.complexo.domain.repository;

import br.com.santosschein.complexo.domain.model.Reservation;
import br.com.santosschein.complexo.domain.model.ReservationStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository {

    Optional<Reservation> findNextActiveReservation(Long userId, LocalDate currentDate, LocalTime currentTime);

    List<Reservation> findFutureReservationsByUser(Long userId, LocalDate currentDate, LocalTime currentTime);

    List<Reservation> findPastReservationsByUser(Long userId, LocalDate currentDate, LocalTime currentTime);

    List<LocalDate> findAvailableDaysBetween(LocalDate monthStart, LocalDate monthEnd);

    boolean existsByFieldSlotDateAndStatus(
            Long fieldId,
            Long slotId,
            LocalDate reservationDate,
            ReservationStatus status
    );

    Optional<Reservation> findById(Long reservationId);

    Optional<Reservation> findByIdAndUserId(Long reservationId, Long userId);

    Reservation save(Reservation reservation);
}
