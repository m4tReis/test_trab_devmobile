package br.com.santosschein.complexo.application.usecase;

import br.com.santosschein.complexo.domain.model.Reservation;
import br.com.santosschein.complexo.domain.model.ReservationSlot;
import br.com.santosschein.complexo.domain.model.ReservationStatus;
import br.com.santosschein.complexo.domain.model.SoccerField;
import br.com.santosschein.complexo.domain.model.User;
import br.com.santosschein.complexo.domain.repository.ReservationRepository;
import br.com.santosschein.complexo.infrastructure.config.AppProperties;
import br.com.santosschein.complexo.shared.exception.ForbiddenOperationException;
import br.com.santosschein.complexo.shared.exception.ReservationUnavailableException;
import br.com.santosschein.complexo.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CancelReservationUseCaseTest {

    @Mock
    private ReservationRepository reservationRepository;

    private CancelReservationUseCase useCase;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-07-10T16:30:00Z"), ZoneId.of("UTC"));
        AppProperties appProperties = new AppProperties(
                new AppProperties.Jwt("test-secret-with-enough-length-for-hmac-signing", 3600),
                new AppProperties.Reservation(60, 24)
        );
        useCase = new CancelReservationUseCase(reservationRepository, clock, appProperties);
    }

    @Test
    void cancelsReservationSuccessfully() {
        // Agora: 10/07 16:30. Reserva: 11/07 19:00 (26h30 de antecedencia).
        when(reservationRepository.findById(123L))
                .thenReturn(Optional.of(reservation(123L, 99L, ReservationStatus.ACTIVE, LocalDate.of(2026, 7, 11), LocalTime.of(19, 0))));

        useCase.execute(99L, "res_123");

        ArgumentCaptor<Reservation> reservationCaptor = ArgumentCaptor.forClass(Reservation.class);
        verify(reservationRepository).save(reservationCaptor.capture());
        assertThat(reservationCaptor.getValue().status()).isEqualTo(ReservationStatus.CANCELED);
        assertThat(reservationCaptor.getValue().canceledAt()).isEqualTo(LocalDateTime.of(2026, 7, 10, 16, 30));
    }

    @Test
    void rejectsCancelWhenReservationDoesNotExist() {
        when(reservationRepository.findById(123L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(99L, "res_123"))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(reservationRepository, never()).save(any());
    }

    @Test
    void rejectsCancelWhenReservationBelongsToAnotherUser() {
        when(reservationRepository.findById(123L))
                .thenReturn(Optional.of(reservation(123L, 77L, ReservationStatus.ACTIVE, LocalDate.of(2026, 7, 10), LocalTime.of(19, 0))));

        assertThatThrownBy(() -> useCase.execute(99L, "res_123"))
                .isInstanceOf(ForbiddenOperationException.class);

        verify(reservationRepository, never()).save(any());
    }

    @Test
    void allowsCancelExactly24HoursBefore() {
        // Agora: 10/07 16:30. Reserva: 11/07 16:30 (exatamente 24h).
        when(reservationRepository.findById(123L))
                .thenReturn(Optional.of(reservation(123L, 99L, ReservationStatus.ACTIVE, LocalDate.of(2026, 7, 11), LocalTime.of(16, 30))));

        useCase.execute(99L, "res_123");

        verify(reservationRepository).save(any());
    }

    @Test
    void rejectsCancelWithLessThan24HoursNotice() {
        // Agora: 10/07 16:30. Reserva: 11/07 16:00 (23h30 de antecedencia).
        when(reservationRepository.findById(123L))
                .thenReturn(Optional.of(reservation(123L, 99L, ReservationStatus.ACTIVE, LocalDate.of(2026, 7, 11), LocalTime.of(16, 0))));

        assertThatThrownBy(() -> useCase.execute(99L, "res_123"))
                .isInstanceOf(ReservationUnavailableException.class)
                .hasMessage("Prazo de cancelamento encerrado.");

        verify(reservationRepository, never()).save(any());
    }

    @Test
    void rejectsCancelWhenReservationIsAlreadyCanceled() {
        when(reservationRepository.findById(123L))
                .thenReturn(Optional.of(reservation(123L, 99L, ReservationStatus.CANCELED, LocalDate.of(2026, 7, 10), LocalTime.of(19, 0))));

        assertThatThrownBy(() -> useCase.execute(99L, "res_123"))
                .isInstanceOf(ReservationUnavailableException.class)
                .hasMessage("Reserva ja cancelada.");

        verify(reservationRepository, never()).save(any());
    }

    private Reservation reservation(Long id, Long userId, ReservationStatus status, LocalDate date, LocalTime startTime) {
        LocalDateTime now = LocalDateTime.of(2026, 7, 1, 10, 0);
        SoccerField field = new SoccerField(1L, "Campo 1", true, now, now);
        User user = new User(
                userId,
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
                startTime,
                startTime.plusHours(1),
                new BigDecimal("150.00"),
                true,
                now,
                now
        );
        return new Reservation(id, user, field, slot, date, status, now, status == ReservationStatus.CANCELED ? now : null);
    }
}
