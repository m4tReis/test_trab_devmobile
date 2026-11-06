package br.com.santosschein.complexo.application.usecase;

import br.com.santosschein.complexo.application.service.ReservationResponseMapper;
import br.com.santosschein.complexo.domain.repository.ReservationRepository;
import br.com.santosschein.complexo.interfaces.dto.response.ReservationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GetNextReservationUseCase {

    private final ReservationRepository reservationRepository;
    private final ReservationResponseMapper reservationResponseMapper;
    private final Clock clock;

    public Optional<ReservationResponse> execute(Long authenticatedUserId) {
        return reservationRepository.findNextActiveReservation(
                        authenticatedUserId,
                        LocalDate.now(clock),
                        LocalTime.now(clock)
                )
                .map(reservationResponseMapper::toResponse);
    }
}
