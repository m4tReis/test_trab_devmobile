package br.com.santosschein.complexo.application.usecase;

import br.com.santosschein.complexo.domain.model.ReservationSlot;
import br.com.santosschein.complexo.domain.model.SoccerField;
import br.com.santosschein.complexo.domain.repository.ReservationSlotRepository;
import br.com.santosschein.complexo.shared.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetAvailableSlotsUseCaseTest {

    @Mock
    private ReservationSlotRepository reservationSlotRepository;

    @InjectMocks
    private GetAvailableSlotsUseCase useCase;

    @Test
    void returnsAvailableSlotsForDate() {
        LocalDate date = LocalDate.of(2026, 7, 10);
        when(reservationSlotRepository.findAvailableSlotsByDate(date)).thenReturn(List.of(slot(1L)));

        var response = useCase.execute(date);

        assertThat(response.slots()).hasSize(1);
        assertThat(response.slots().getFirst().id()).isEqualTo("slot_1");
        assertThat(response.slots().getFirst().startTime()).isEqualTo(LocalTime.of(18, 0));
        assertThat(response.slots().getFirst().endTime()).isEqualTo(LocalTime.of(19, 0));
        assertThat(response.slots().getFirst().price()).isEqualByComparingTo("150.00");
    }

    @Test
    void returnsEmptyListWhenDateHasNoAvailableSlots() {
        LocalDate date = LocalDate.of(2026, 7, 10);
        when(reservationSlotRepository.findAvailableSlotsByDate(date)).thenReturn(List.of());

        var response = useCase.execute(date);

        assertThat(response.slots()).isEmpty();
    }

    @Test
    void canceledReservationsDoNotBlockAvailability() {
        LocalDate date = LocalDate.of(2026, 7, 10);
        when(reservationSlotRepository.findAvailableSlotsByDate(date)).thenReturn(List.of(slot(2L)));

        var response = useCase.execute(date);

        assertThat(response.slots())
                .extracting(slot -> slot.id())
                .containsExactly("slot_2");
    }

    @Test
    void rejectsInvalidDate() {
        assertThatThrownBy(() -> useCase.execute(null))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Data invalida.");
    }

    private ReservationSlot slot(Long id) {
        LocalDateTime now = LocalDateTime.now();
        SoccerField field = new SoccerField(1L, "Campo 1", true, now, now);
        return new ReservationSlot(
                id,
                field,
                LocalTime.of(18, 0),
                LocalTime.of(19, 0),
                new BigDecimal("150.00"),
                true,
                now,
                now
        );
    }
}
