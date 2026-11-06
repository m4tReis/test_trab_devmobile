package br.com.santosschein.complexo.testsupport;

import br.com.santosschein.complexo.domain.model.Reservation;
import br.com.santosschein.complexo.domain.model.ReservationSlot;
import br.com.santosschein.complexo.domain.model.ReservationStatus;
import br.com.santosschein.complexo.domain.model.SoccerField;
import br.com.santosschein.complexo.domain.model.User;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public final class TestDataFactory {

    private TestDataFactory() {
    }

    public static User user(Long id) {
        LocalDateTime now = LocalDateTime.of(2026, 7, 1, 10, 0);
        return new User(
                id,
                "Joao",
                "Silva",
                LocalDate.of(1995, 4, 20),
                "12345678900",
                "joao@email.com",
                "$2a$hash",
                now,
                now
        );
    }

    public static SoccerField field(Long id, String name) {
        LocalDateTime now = LocalDateTime.of(2026, 7, 1, 10, 0);
        return new SoccerField(id, name, true, now, now);
    }

    public static ReservationSlot slot(Long id, SoccerField field) {
        LocalDateTime now = LocalDateTime.of(2026, 7, 1, 10, 0);
        return new ReservationSlot(
                id,
                field,
                LocalTime.of(19, 0),
                LocalTime.of(20, 0),
                new BigDecimal("150.00"),
                true,
                now,
                now
        );
    }

    public static Reservation reservation(Long id, User user, ReservationStatus status, LocalDate date) {
        LocalDateTime now = LocalDateTime.of(2026, 7, 1, 10, 0);
        SoccerField field = field(1L, "Campo 1");
        ReservationSlot slot = slot(1L, field);
        return new Reservation(
                id,
                user,
                field,
                slot,
                date,
                status,
                now,
                status == ReservationStatus.CANCELED ? now : null
        );
    }
}
