package br.com.santosschein.complexo.application.usecase;

import br.com.santosschein.complexo.domain.repository.ReservationSlotRepository;
import br.com.santosschein.complexo.interfaces.dto.response.AvailableDaysResponse;
import br.com.santosschein.complexo.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GetAvailableDaysUseCase {

    private static final int MIN_YEAR = 1;
    private static final int MAX_YEAR = 9999;

    private final ReservationSlotRepository reservationSlotRepository;

    public AvailableDaysResponse execute(Integer year, Integer month) {
        YearMonth yearMonth = validateAndCreateYearMonth(year, month);
        LocalDate monthStart = yearMonth.atDay(1);
        LocalDate monthEnd = yearMonth.atEndOfMonth();
        List<LocalDate> availableDays = reservationSlotRepository.findAvailableDaysBetween(monthStart, monthEnd);

        return new AvailableDaysResponse(availableDays);
    }

    private YearMonth validateAndCreateYearMonth(Integer year, Integer month) {
        if (year == null || year < MIN_YEAR || year > MAX_YEAR) {
            throw new BusinessException("Ano invalido.");
        }

        if (month == null || month < 1 || month > 12) {
            throw new BusinessException("Mes invalido.");
        }

        try {
            return YearMonth.of(year, month);
        } catch (DateTimeException exception) {
            throw new BusinessException("Mes ou ano invalido.");
        }
    }
}
