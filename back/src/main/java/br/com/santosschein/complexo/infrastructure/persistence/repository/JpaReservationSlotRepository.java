package br.com.santosschein.complexo.infrastructure.persistence.repository;

import br.com.santosschein.complexo.infrastructure.persistence.entity.ReservationSlotEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface JpaReservationSlotRepository extends JpaRepository<ReservationSlotEntity, Long> {

    @Query(
            value = """
                    SELECT TO_CHAR(available_days.day_value, 'YYYY-MM-DD')
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
    List<String> findAvailableDaysBetween(
            @Param("monthStart") LocalDate monthStart,
            @Param("monthEnd") LocalDate monthEnd
    );

    @Query(
            value = """
                    SELECT rs.*
                    FROM reservation_slots rs
                    JOIN soccer_fields sf ON sf.id = rs.field_id
                    WHERE rs.id IN (
                        SELECT MIN(available_rs.id)
                        FROM reservation_slots available_rs
                        JOIN soccer_fields available_sf ON available_sf.id = available_rs.field_id
                        WHERE available_rs.active = 1
                          AND available_sf.active = 1
                          AND NOT EXISTS (
                              SELECT 1
                              FROM reservations r
                              WHERE r.field_id = available_rs.field_id
                                AND r.slot_id = available_rs.id
                                AND TRUNC(r.reservation_date) = TRUNC(:reservationDate)
                                AND r.status = 'ACTIVE'
                          )
                        GROUP BY available_rs.start_time, available_rs.end_time, available_rs.price
                      )
                    ORDER BY rs.start_time
                    """,
            nativeQuery = true
    )
    List<ReservationSlotEntity> findAvailableSlotsByDate(@Param("reservationDate") LocalDate reservationDate);

    @Query(
            value = """
                    SELECT candidate_rs.*
                    FROM reservation_slots requested_rs
                    JOIN reservation_slots candidate_rs
                      ON candidate_rs.start_time = requested_rs.start_time
                     AND candidate_rs.end_time = requested_rs.end_time
                     AND candidate_rs.price = requested_rs.price
                    JOIN soccer_fields candidate_sf ON candidate_sf.id = candidate_rs.field_id
                    WHERE requested_rs.id = :slotId
                      AND requested_rs.active = 1
                      AND candidate_rs.active = 1
                      AND candidate_sf.active = 1
                      AND NOT EXISTS (
                          SELECT 1
                          FROM reservations r
                          WHERE r.field_id = candidate_rs.field_id
                            AND r.slot_id = candidate_rs.id
                            AND TRUNC(r.reservation_date) = TRUNC(:reservationDate)
                            AND r.status = 'ACTIVE'
                      )
                    ORDER BY candidate_sf.id ASC, candidate_sf.name ASC
                    FETCH FIRST 1 ROWS ONLY
                    """,
            nativeQuery = true
    )
    Optional<ReservationSlotEntity> findFirstAvailableEquivalentSlot(
            @Param("slotId") Long slotId,
            @Param("reservationDate") LocalDate reservationDate
    );
}
