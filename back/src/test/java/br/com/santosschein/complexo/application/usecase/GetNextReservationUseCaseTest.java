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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetNextReservationUseCaseTest {

    @Mock
    private ReservationRepository reservationRepository;

    private GetNextReservationUseCase useCase;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-07-10T15:30:00Z"), ZoneId.of("UTC"));
        useCase = new GetNextReservationUseCase(
                reservationRepository,
                new ReservationResponseMapper(),
                clock
        );
    }

    @Test
    void returnsNextReservationWhenItExists() {
        Reservation reservation = reservation(123L, user(99L), ReservationStatus.ACTIVE, LocalDate.of(2026, 7, 10));
        when(reservationRepository.findNextActiveReservation(
                99L,
                LocalDate.of(2026, 7, 10),
                LocalTime.of(15, 30)
        )).thenReturn(Optional.of(reservation));

        var response = useCase.execute(99L);

        assertThat(response).isPresent();
        assertThat(response.get().id()).isEqualTo("res_123");
        assertThat(response.get().fieldName()).isEqualTo("Campo 1");
        assertThat(response.get().status()).isEqualTo(ReservationStatus.ACTIVE);
    }

    @Test
    void returnsEmptyWhenThereIsNoNextReservation() {
        when(reservationRepository.findNextActiveReservation(
                99L,
                LocalDate.of(2026, 7, 10),
                LocalTime.of(15, 30)
        )).thenReturn(Optional.empty());

        assertThat(useCase.execute(99L)).isEmpty();
    }

    private Reservation reservation(Long id, User user, ReservationStatus status, LocalDate date) {
        LocalDateTime now = LocalDateTime.now();
        SoccerField field = new SoccerField(1L, "Campo 1", true, now, now);
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
        return new Reservation(id, user, field, slot, date, status, now, null);
    }

    private User user(Long id) {
        LocalDateTime now = LocalDateTime.now();
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
}
