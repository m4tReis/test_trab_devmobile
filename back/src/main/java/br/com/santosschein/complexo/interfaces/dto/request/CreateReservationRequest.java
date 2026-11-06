package br.com.santosschein.complexo.interfaces.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateReservationRequest(
        @NotNull
        LocalDate date,

        @NotBlank
        String slotId
) {
}
