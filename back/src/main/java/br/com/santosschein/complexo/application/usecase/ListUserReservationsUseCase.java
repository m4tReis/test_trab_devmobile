package br.com.santosschein.complexo.application.usecase;

import br.com.santosschein.complexo.application.service.ReservationResponseMapper;
import br.com.santosschein.complexo.domain.model.Reservation;
import br.com.santosschein.complexo.domain.repository.ReservationRepository;
import br.com.santosschein.complexo.interfaces.dto.response.ReservationsResponse;
import br.com.santosschein.complexo.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ListUserReservationsUseCase {

    private static final String UPCOMING = "upcoming";
    private static final String PAST = "past";

    private final ReservationRepository reservationRepository;
    private final ReservationResponseMapper reservationResponseMapper;
    private final Clock clock;

    public ReservationsResponse execute(Long authenticatedUserId, String status) {
        LocalDate currentDate = LocalDate.now(clock);
        LocalTime currentTime = LocalTime.now(clock);
        List<Reservation> reservations = switch (normalize(status)) {
            case UPCOMING -> reservationRepository.findFutureReservationsByUser(
                    authenticatedUserId,
                    currentDate,
                    currentTime
            );
            case PAST -> reservationRepository.findPastReservationsByUser(
                    authenticatedUserId,
                    currentDate,
                    currentTime
            );
            default -> throw new BusinessException("Status de consulta invalido.");
        };

        LocalDateTime now = LocalDateTime.of(currentDate, currentTime);
        return new ReservationsResponse(
                reservations.stream()
                        .map(reservation -> reservationResponseMapper.toResponse(reservation, now))
                        .toList()
        );
    }

    private String normalize(String status) {
        if (status == null || status.isBlank()) {
            return UPCOMING;
        }

        return status.toLowerCase();
    }
}
