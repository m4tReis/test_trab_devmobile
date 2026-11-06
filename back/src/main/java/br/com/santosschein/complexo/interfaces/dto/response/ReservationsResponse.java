package br.com.santosschein.complexo.interfaces.dto.response;

import java.util.List;

public record ReservationsResponse(
        List<ReservationResponse> reservations
) {
}
