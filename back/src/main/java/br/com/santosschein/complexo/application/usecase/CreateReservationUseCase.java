package br.com.santosschein.complexo.application.usecase;

import br.com.santosschein.complexo.domain.model.Reservation;
import br.com.santosschein.complexo.domain.model.ReservationSlot;
import br.com.santosschein.complexo.domain.model.ReservationStatus;
import br.com.santosschein.complexo.domain.model.User;
import br.com.santosschein.complexo.domain.repository.ReservationRepository;
import br.com.santosschein.complexo.domain.repository.ReservationSlotRepository;
import br.com.santosschein.complexo.domain.repository.UserRepository;
import br.com.santosschein.complexo.application.service.ReservationResponseMapper;
import br.com.santosschein.complexo.interfaces.dto.request.CreateReservationRequest;
import br.com.santosschein.complexo.interfaces.dto.response.ReservationResponse;
import br.com.santosschein.complexo.shared.exception.BusinessException;
import br.com.santosschein.complexo.shared.exception.InvalidCredentialsException;
import br.com.santosschein.complexo.shared.exception.ReservationUnavailableException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CreateReservationUseCase {

    private static final String SLOT_ID_PREFIX = "slot_";

    private final UserRepository userRepository;
    private final ReservationSlotRepository reservationSlotRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationResponseMapper reservationResponseMapper;
    private final Clock clock;

    @Transactional
    public ReservationResponse execute(Long authenticatedUserId, CreateReservationRequest request) {
        if (request.date() == null) {
            throw new BusinessException("Data invalida.");
        }

        LocalDate today = LocalDate.now(clock);
        if (request.date().isBefore(today)) {
            throw new BusinessException("Nao e permitido criar reserva em data passada.");
        }

        Long requestedSlotId = parseSlotId(request.slotId());
        ReservationSlot requestedSlot = reservationSlotRepository.findById(requestedSlotId)
                .filter(ReservationSlot::active)
                .orElseThrow(() -> new ReservationUnavailableException("Horario indisponivel."));

        User user = userRepository.findById(authenticatedUserId)
                .orElseThrow(InvalidCredentialsException::new);

        ReservationSlot availableSlot = reservationSlotRepository
                .findFirstAvailableEquivalentSlot(requestedSlot.id(), request.date())
                .orElseThrow(() -> new ReservationUnavailableException("Horario indisponivel."));

        Reservation reservation = new Reservation(
                null,
                user,
                availableSlot.field(),
                availableSlot,
                request.date(),
                ReservationStatus.ACTIVE,
                LocalDateTime.now(clock),
                null,
                // Copia do valor no momento da confirmacao (o preco nao vem do cliente).
                availableSlot.price()
        );

        try {
            return reservationResponseMapper.toResponse(reservationRepository.save(reservation));
        } catch (DataIntegrityViolationException exception) {
            throw new ReservationUnavailableException("Horario indisponivel.");
        }
    }

    private Long parseSlotId(String slotId) {
        if (slotId == null || slotId.isBlank()) {
            throw new BusinessException("Slot invalido.");
        }

        if (!slotId.startsWith(SLOT_ID_PREFIX)) {
            throw new BusinessException("Slot invalido.");
        }

        try {
            return Long.valueOf(slotId.substring(SLOT_ID_PREFIX.length()));
        } catch (NumberFormatException exception) {
            throw new BusinessException("Slot invalido.");
        }
    }

}
