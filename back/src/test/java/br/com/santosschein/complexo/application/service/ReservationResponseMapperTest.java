package br.com.santosschein.complexo.application.service;

import br.com.santosschein.complexo.domain.model.Reservation;
import br.com.santosschein.complexo.domain.model.ReservationSlot;
import br.com.santosschein.complexo.domain.model.ReservationStatus;
import br.com.santosschein.complexo.domain.model.SoccerField;
import br.com.santosschein.complexo.domain.model.User;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationResponseMapperTest {

    private final ReservationResponseMapper mapper = new ReservationResponseMapper();

    @Test
    void usesPriceCopiedAtConfirmationEvenIfSlotPriceChanged() {
        // Reserva confirmada a R$ 150,00; depois o horario passou a custar R$ 180,00.
        Reservation reservation = reservation(ReservationStatus.ACTIVE, new BigDecimal("180.00"), new BigDecimal("150.00"));

        assertThat(mapper.toResponse(reservation).price()).isEqualByComparingTo("150.00");
    }

    @Test
    void activeReservationThatAlreadyEndedIsShownAsCompleted() {
        Reservation reservation = reservation(ReservationStatus.ACTIVE, new BigDecimal("150.00"), new BigDecimal("150.00"));

        // Horario 19:00-20:00 em 10/07.
        assertThat(mapper.toResponse(reservation, LocalDateTime.of(2026, 7, 10, 20, 0)).status())
                .isEqualTo(ReservationStatus.COMPLETED);
        assertThat(mapper.toResponse(reservation, LocalDateTime.of(2026, 7, 10, 19, 30)).status())
                .isEqualTo(ReservationStatus.ACTIVE);
        assertThat(mapper.toResponse(reservation).status())
                .isEqualTo(ReservationStatus.ACTIVE);
    }

    @Test
    void canceledReservationStaysCanceled() {
        Reservation reservation = reservation(ReservationStatus.CANCELED, new BigDecimal("150.00"), new BigDecimal("150.00"));

        assertThat(mapper.toResponse(reservation, LocalDateTime.of(2026, 7, 11, 0, 0)).status())
                .isEqualTo(ReservationStatus.CANCELED);
    }

    private Reservation reservation(ReservationStatus status, BigDecimal currentSlotPrice, BigDecimal chargedPrice) {
        LocalDateTime now = LocalDateTime.of(2026, 7, 1, 10, 0);
        SoccerField field = new SoccerField(1L, "Campo 1", true, now, now);
        User user = new User(99L, "Joao", "Silva", LocalDate.of(1995, 4, 20), "12345678900",
                "joao@email.com", "$2a$hash", now, now);
        ReservationSlot slot = new ReservationSlot(1L, field, LocalTime.of(19, 0), LocalTime.of(20, 0),
                currentSlotPrice, true, now, now);
        return new Reservation(10L, user, field, slot, LocalDate.of(2026, 7, 10), status, now, null, chargedPrice);
    }
}
