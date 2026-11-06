package br.com.santosschein.complexo.application.usecase;

import br.com.santosschein.complexo.application.service.ReservationResponseMapper;
import br.com.santosschein.complexo.domain.model.Reservation;
import br.com.santosschein.complexo.domain.model.ReservationSlot;
import br.com.santosschein.complexo.domain.model.ReservationStatus;
import br.com.santosschein.complexo.domain.model.SoccerField;
import br.com.santosschein.complexo.domain.model.User;
import br.com.santosschein.complexo.domain.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListUserReservationsUseCaseTest {

    @Mock
    private ReservationRepository reservationRepository;

    private ListUserReservationsUseCase useCase;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-07-10T15:30:00Z"), ZoneId.of("UTC"));
        useCase = new ListUserReservationsUseCase(
                reservationRepository,
                new ReservationResponseMapper(),
                clock
        );
    }

    @Test
    void listsUpcomingReservations() {
        Reservation reservation = reservation(123L, ReservationStatus.ACTIVE, LocalDate.of(2026, 7, 10));
        when(reservationRepository.findFutureReservationsByUser(
                99L,
                LocalDate.of(2026, 7, 10),
                LocalTime.of(15, 30)
        )).thenReturn(List.of(reservation));

        var response = useCase.execute(99L, "upcoming");

        assertThat(response.reservations()).hasSize(1);
        assertThat(response.reservations().getFirst().id()).isEqualTo("res_123");
        verify(reservationRepository).findFutureReservationsByUser(
                99L,
                LocalDate.of(2026, 7, 10),
                LocalTime.of(15, 30)
        );
    }

    @Test
    void listsPastReservations() {
        Reservation reservation = reservation(124L, ReservationStatus.CANCELED, LocalDate.of(2026, 7, 9));
        when(reservationRepository.findPastReservationsByUser(
                99L,
                LocalDate.of(2026, 7, 10),
                LocalTime.of(15, 30)
        )).thenReturn(List.of(reservation));

        var response = useCase.execute(99L, "past");

        assertThat(response.reservations()).hasSize(1);
        assertThat(response.reservations().getFirst().id()).isEqualTo("res_124");
        assertThat(response.reservations().getFirst().status()).isEqualTo(ReservationStatus.CANCELED);
    }

    private Reservation reservation(Long id, ReservationStatus status, LocalDate date) {
        LocalDateTime now = LocalDateTime.now();
        SoccerField field = new SoccerField(1L, "Campo 1", true, now, now);
        User user = new User(
                99L,
                "Joao",
                "Silva",
                LocalDate.of(1995, 4, 20),
                "12345678900",
                "joao@email.com",
                "$2a$hash",
                now,
                now
        );
        ReservationSlot slot = new ReservationSlot(
                1L,
                field,
                LocalTime.of(19, 0),
                LocalTime.of(20, 0),
                new BigDecimal("150.00"),
                true,
                now,
                now
        );
        return new Reservation(id, user, field, slot, date, status, now, status == ReservationStatus.CANCELED ? now : null);
    }
}
