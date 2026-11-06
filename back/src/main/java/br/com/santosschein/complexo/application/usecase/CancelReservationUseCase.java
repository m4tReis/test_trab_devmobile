package br.com.santosschein.complexo.application.usecase;

import br.com.santosschein.complexo.domain.model.Reservation;
import br.com.santosschein.complexo.domain.model.ReservationStatus;
import br.com.santosschein.complexo.domain.repository.ReservationRepository;
import br.com.santosschein.complexo.infrastructure.config.AppProperties;
import br.com.santosschein.complexo.shared.exception.BusinessException;
import br.com.santosschein.complexo.shared.exception.ForbiddenOperationException;
import br.com.santosschein.complexo.shared.exception.ReservationUnavailableException;
import br.com.santosschein.complexo.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CancelReservationUseCase {

    private static final String RESERVATION_ID_PREFIX = "res_";

    private final ReservationRepository reservationRepository;
    private final Clock clock;
    private final AppProperties appProperties;

    @Transactional
    public void execute(Long authenticatedUserId, String reservationId) {
        Long parsedReservationId = parseReservationId(reservationId);
        Reservation reservation = reservationRepository.findById(parsedReservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva nao encontrada."));

        if (!reservation.user().id().equals(authenticatedUserId)) {
            throw new ForbiddenOperationException("Reserva pertence a outro usuario.");
        }

        if (reservation.status() == ReservationStatus.CANCELED) {
            throw new ReservationUnavailableException("Reserva ja cancelada.");
        }

        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime reservationStart = LocalDateTime.of(
                reservation.reservationDate(),
                reservation.slot().startTime()
        );
        // RF07: cancelamento permitido somente com a antecedencia minima configurada (24 h no relatorio).
        LocalDateTime cancelDeadline = reservationStart.minusHours(appProperties.reservation().minimumCancelHours());
        if (now.isAfter(cancelDeadline)) {
            throw new ReservationUnavailableException("Prazo de cancelamento encerrado.");
        }

        Reservation canceledReservation = new Reservation(
                reservation.id(),
                reservation.user(),
                reservation.field(),
                reservation.slot(),
                reservation.reservationDate(),
                ReservationStatus.CANCELED,
                reservation.createdAt(),
                now,
                reservation.chargedPrice()
        );

        reservationRepository.save(canceledReservation);
    }

    private Long parseReservationId(String reservationId) {
        if (reservationId == null || reservationId.isBlank() || !reservationId.startsWith(RESERVATION_ID_PREFIX)) {
            throw new BusinessException("Id da reserva invalido.");
        }

        try {
            return Long.valueOf(reservationId.substring(RESERVATION_ID_PREFIX.length()));
        } catch (NumberFormatException exception) {
            throw new BusinessException("Id da reserva invalido.");
        }
    }
}
