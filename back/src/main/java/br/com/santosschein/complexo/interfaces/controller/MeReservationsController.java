package br.com.santosschein.complexo.interfaces.controller;

import br.com.santosschein.complexo.application.usecase.CancelReservationUseCase;
import br.com.santosschein.complexo.application.usecase.GetNextReservationUseCase;
import br.com.santosschein.complexo.application.usecase.ListUserReservationsUseCase;
import br.com.santosschein.complexo.interfaces.dto.response.ReservationResponse;
import br.com.santosschein.complexo.interfaces.dto.response.ReservationsResponse;
import br.com.santosschein.complexo.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/me/reservations")
@RequiredArgsConstructor
public class MeReservationsController {

    private final GetNextReservationUseCase getNextReservationUseCase;
    private final ListUserReservationsUseCase listUserReservationsUseCase;
    private final CancelReservationUseCase cancelReservationUseCase;

    @GetMapping("/next")
    public ResponseEntity<ReservationResponse> next(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return getNextReservationUseCase.execute(authenticatedUser.id())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping
    public ReservationsResponse list(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @RequestParam(defaultValue = "upcoming") String status
    ) {
        return listUserReservationsUseCase.execute(authenticatedUser.id(), status);
    }

    @DeleteMapping("/{reservationId}")
    public ResponseEntity<Void> cancel(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable String reservationId
    ) {
        cancelReservationUseCase.execute(authenticatedUser.id(), reservationId);
        return ResponseEntity.noContent().build();
    }
}
