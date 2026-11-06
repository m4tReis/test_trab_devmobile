package br.com.santosschein.complexo.interfaces.controller;

import br.com.santosschein.complexo.application.usecase.CreateReservationUseCase;
import br.com.santosschein.complexo.application.usecase.GetAvailableDaysUseCase;
import br.com.santosschein.complexo.application.usecase.GetAvailableSlotsUseCase;
import br.com.santosschein.complexo.interfaces.dto.request.CreateReservationRequest;
import br.com.santosschein.complexo.interfaces.dto.response.AvailableDaysResponse;
import br.com.santosschein.complexo.interfaces.dto.response.AvailableSlotsResponse;
import br.com.santosschein.complexo.interfaces.dto.response.ReservationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import br.com.santosschein.complexo.security.AuthenticatedUser;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
public class ReservationAvailabilityController {

    private final CreateReservationUseCase createReservationUseCase;
    private final GetAvailableDaysUseCase getAvailableDaysUseCase;
    private final GetAvailableSlotsUseCase getAvailableSlotsUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse create(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @Valid @RequestBody CreateReservationRequest request
    ) {
        return createReservationUseCase.execute(authenticatedUser.id(), request);
    }

    @GetMapping("/available-days")
    public AvailableDaysResponse availableDays(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @RequestParam Integer year,
            @RequestParam Integer month
    ) {
        authenticatedUser.id();
        return getAvailableDaysUseCase.execute(year, month);
    }

    @GetMapping("/available-slots")
    public AvailableSlotsResponse availableSlots(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        authenticatedUser.id();
        return getAvailableSlotsUseCase.execute(date);
    }
}
