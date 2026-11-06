package br.com.santosschein.complexo.infrastructure.persistence.repository;

import br.com.santosschein.complexo.domain.model.ReservationStatus;
import br.com.santosschein.complexo.infrastructure.persistence.entity.ReservationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface JpaReservationRepository extends JpaRepository<ReservationEntity, Long> {

    @Query(
            value = """
                    SELECT r.*
                    FROM reservations r
                    JOIN reservation_slots rs ON rs.id = r.slot_id
                    WHERE r.user_id = :userId
                      AND r.status = 'ACTIVE'
                      AND (
                          TRUNC(r.reservation_date) > TRUNC(:currentDate)
                          OR (
                              TRUNC(r.reservation_date) = TRUNC(:currentDate)
                              AND rs.start_time > :currentTime
                          )
                      )
                    ORDER BY r.reservation_date ASC, rs.start_time ASC
                    FETCH FIRST 1 ROWS ONLY
                    """,
            nativeQuery = true
    )
    Optional<ReservationEntity> findNextActiveReservation(
            @Param("userId") Long userId,
            @Param("currentDate") LocalDate currentDate,
            @Param("currentTime") String currentTime
    );

    @Query(
            value = """
                    SELECT r.*
                    FROM reservations r
                    JOIN reservation_slots rs ON rs.id = r.slot_id
                    WHERE r.user_id = :userId
                      AND r.status = 'ACTIVE'
                      AND (
                          TRUNC(r.reservation_date) > TRUNC(:currentDate)
                          OR (
                              TRUNC(r.reservation_date) = TRUNC(:currentDate)
                              AND rs.start_time > :currentTime
                          )
                      )
                    ORDER BY r.reservation_date ASC, rs.start_time ASC
                    """,
            nativeQuery = true
    )
    List<ReservationEntity> findFutureReservationsByUser(
            @Param("userId") Long userId,
            @Param("currentDate") LocalDate currentDate,
            @Param("currentTime") String currentTime
    );

    @Query(
            value = """
                    SELECT r.*
                    FROM reservations r
                    JOIN reservation_slots rs ON rs.id = r.slot_id
                    WHERE r.user_id = :userId
                      AND (
                          TRUNC(r.reservation_date) < TRUNC(:currentDate)
                          OR (
                              TRUNC(r.reservation_date) = TRUNC(:currentDate)
                              AND rs.start_time < :currentTime
                          )
                      )
                    ORDER BY r.reservation_date DESC, rs.start_time DESC
                    """,
            nativeQuery = true
    )
    List<ReservationEntity> findPastReservationsByUser(
            @Param("userId") Long userId,
            @Param("currentDate") LocalDate currentDate,
            @Param("currentTime") String currentTime
    );

    @Query(
            value = """
                    SELECT available_days.day_value
                    FROM (
                        SELECT TRUNC(:monthStart) + LEVEL - 1 AS day_value
                        FROM dual
                        CONNECT BY LEVEL <= TRUNC(:monthEnd) - TRUNC(:monthStart) + 1
                    ) available_days
                    WHERE EXISTS (
                        SELECT 1
                        FROM reservation_slots rs
                        JOIN soccer_fields sf ON sf.id = rs.field_id
                        WHERE rs.active = 1
                          AND sf.active = 1
                          AND NOT EXISTS (
                              SELECT 1
                              FROM reservations r
                              WHERE r.field_id = rs.field_id
                                AND r.slot_id = rs.id
                                AND TRUNC(r.reservation_date) = available_days.day_value
                                AND r.status = 'ACTIVE'
                          )
                    )
                    ORDER BY available_days.day_value
                    """,
            nativeQuery = true
    )
    List<LocalDate> findAvailableDaysBetween(
            @Param("monthStart") LocalDate monthStart,
            @Param("monthEnd") LocalDate monthEnd
    );

    boolean existsByField_IdAndSlot_IdAndReservationDateAndStatus(
            Long fieldId,
            Long slotId,
            LocalDate reservationDate,
            ReservationStatus status
    );

    Optional<ReservationEntity> findByIdAndUser_Id(Long reservationId, Long userId);
}
