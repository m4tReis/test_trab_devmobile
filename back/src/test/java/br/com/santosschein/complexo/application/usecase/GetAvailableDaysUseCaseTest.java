package br.com.santosschein.complexo.application.usecase;

import br.com.santosschein.complexo.domain.repository.ReservationSlotRepository;
import br.com.santosschein.complexo.shared.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetAvailableDaysUseCaseTest {

    @Mock
    private ReservationSlotRepository reservationSlotRepository;

    @InjectMocks
    private GetAvailableDaysUseCase useCase;

    @Test
    void returnsAvailableDaysForMonth() {
        List<LocalDate> availableDays = List.of(
                LocalDate.of(2026, 7, 10),
                LocalDate.of(2026, 7, 11),
                LocalDate.of(2026, 7, 13)
        );
        when(reservationSlotRepository.findAvailableDaysBetween(
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 31)
        )).thenReturn(availableDays);

        var response = useCase.execute(2026, 7);

        assertThat(response.availableDays()).containsExactlyElementsOf(availableDays);
        verify(reservationSlotRepository).findAvailableDaysBetween(
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 31)
        );
    }

    @Test
    void returnsEmptyListWhenMonthHasNoAvailableDays() {
        when(reservationSlotRepository.findAvailableDaysBetween(
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 31)
        )).thenReturn(List.of());

        var response = useCase.execute(2026, 7);

        assertThat(response.availableDays()).isEmpty();
    }

    @Test
    void rejectsInvalidMonth() {
        assertThatThrownBy(() -> useCase.execute(2026, 13))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Mes invalido.");
    }

    @Test
    void rejectsInvalidYear() {
        assertThatThrownBy(() -> useCase.execute(0, 7))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Ano invalido.");
    }
}
