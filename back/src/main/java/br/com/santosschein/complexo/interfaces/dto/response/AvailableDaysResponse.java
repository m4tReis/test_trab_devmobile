package br.com.santosschein.complexo.interfaces.dto.response;

import java.time.LocalDate;
import java.util.List;

public record AvailableDaysResponse(
        List<LocalDate> availableDays
) {
}
