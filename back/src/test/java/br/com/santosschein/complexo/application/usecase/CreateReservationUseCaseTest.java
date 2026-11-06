package br.com.santosschein.complexo.application.usecase;

import br.com.santosschein.complexo.domain.model.Reservation;
import br.com.santosschein.complexo.domain.model.ReservationSlot;
import br.com.santosschein.complexo.domain.model.ReservationStatus;
import br.com.santosschein.complexo.domain.model.SoccerField;
import br.com.santosschein.complexo.domain.model.User;
import br.com.santosschein.complexo.domain.repository.ReservationRepository;
import br.com.santosschein.complexo.domain.repository.ReservationSlotRepository;
import br.com.santosschein.complexo.domain.repository.UserRepository;
import br.com.santosschein.complexo.application.service.ReservationResponseMapper;
import br.com.santosschein.complexo.interfaces.dto.request.CreateReservationRequest;
import br.com.santosschein.complexo.shared.exception.BusinessException;
import br.com.santosschein.complexo.shared.exception.ReservationUnavailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateReservationUseCaseTest {

    private static final Long AUTHENTICATED_USER_ID = 99L;
    private static final LocalDate RESERVATION_DATE = LocalDate.of(2026, 7, 10);

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReservationSlotRepository reservationSlotRepository;

    @Mock
    private ReservationRepository reservationRepository;

    private CreateReservationUseCase useCase;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-01-01T10:00:00Z"), ZoneId.of("UTC"));
        useCase = new CreateReservationUseCase(
                userRepository,
                reservationSlotRepository,
                reservationRepository,
                new ReservationResponseMapper(),
                clock
        );
    }

    @Test
    void createsReservationSuccessfully() {
        User user = user(AUTHENTICATED_USER_ID);
        ReservationSlot slot = slot(1L, field(1L, "Campo 1"));
        mockSuccessfulReservation(user, slot);

        var response = useCase.execute(AUTHENTICATED_USER_ID, request("slot_1"));

        assertThat(response.id()).isEqualTo("res_123");
        assertThat(response.fieldName()).isEqualTo("Campo 1");
        assertThat(response.date()).isEqualTo(RESERVATION_DATE);
        assertThat(response.startTime()).isEqualTo(LocalTime.of(18, 0));
        assertThat(response.endTime()).isEqualTo(LocalTime.of(19, 0));
        assertThat(response.price()).isEqualByComparingTo("150.00");
        assertThat(response.status()).isEqualTo(ReservationStatus.ACTIVE);
    }

    @Test
    void createsReservationChoosingFirstAvailableFieldWhenMultipleFieldsExist() {
        User user = user(AUTHENTICATED_USER_ID);
        ReservationSlot requestedSlot = slot(10L, field(3L, "Campo Society"));
        ReservationSlot firstAvailableSlot = slot(1L, field(1L, "Campo 1"));
        when(reservationSlotRepository.findById(10L)).thenReturn(Optional.of(requestedSlot));
        when(userRepository.findById(AUTHENTICATED_USER_ID)).thenReturn(Optional.of(user));
        when(reservationSlotRepository.findFirstAvailableEquivalentSlot(10L, RESERVATION_DATE))
                .thenReturn(Optional.of(firstAvailableSlot));
        when(reservationRepository.save(any(Reservation.class)))
                .thenAnswer(invocation -> persisted(invocation.getArgument(0), 124L));

        var response = useCase.execute(AUTHENTICATED_USER_ID, request("slot_10"));

        assertThat(response.fieldName()).isEqualTo("Campo 1");
        verify(reservationSlotRepository).findFirstAvailableEquivalentSlot(10L, RESERVATION_DATE);
    }

    @Test
    void returnsConflictWhenNoFieldIsAvailable() {
        User user = user(AUTHENTICATED_USER_ID);
        ReservationSlot slot = slot(1L, field(1L, "Campo 1"));
        when(reservationSlotRepository.findById(1L)).thenReturn(Optional.of(slot));
        when(userRepository.findById(AUTHENTICATED_USER_ID)).thenReturn(Optional.of(user));
        when(reservationSlotRepository.findFirstAvailableEquivalentSlot(1L, RESERVATION_DATE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(AUTHENTICATED_USER_ID, request("slot_1")))
                .isInstanceOf(ReservationUnavailableException.class);

        verify(reservationRepository, never()).save(any());
    }

    @Test
    void returnsErrorWhenSlotDoesNotExist() {
        when(reservationSlotRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(AUTHENTICATED_USER_ID, request("slot_1")))
                .isInstanceOf(ReservationUnavailableException.class);

        verify(userRepository, never()).findById(AUTHENTICATED_USER_ID);
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void returnsErrorWhenDateIsInThePast() {
        CreateReservationRequest request = new CreateReservationRequest(LocalDate.of(2025, 12, 31), "slot_1");

        assertThatThrownBy(() -> useCase.execute(AUTHENTICATED_USER_ID, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Nao e permitido criar reserva em data passada.");

        verify(reservationSlotRepository, never()).findById(any());
    }

    @Test
    void returnsErrorWhenSlotIdIsBlank() {
        CreateReservationRequest request = new CreateReservationRequest(RESERVATION_DATE, " ");

        assertThatThrownBy(() -> useCase.execute(AUTHENTICATED_USER_ID, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Slot invalido.");

        verify(reservationSlotRepository, never()).findById(any());
    }

    @Test
    void usesAuthenticatedUserWhenCreatingReservation() {
        User user = user(AUTHENTICATED_USER_ID);
        ReservationSlot slot = slot(1L, field(1L, "Campo 1"));
        mockSuccessfulReservation(user, slot);

        useCase.execute(AUTHENTICATED_USER_ID, request("slot_1"));

        ArgumentCaptor<Reservation> reservationCaptor = ArgumentCaptor.forClass(Reservation.class);
        verify(reservationRepository).save(reservationCaptor.capture());
        assertThat(reservationCaptor.getValue().user().id()).isEqualTo(AUTHENTICATED_USER_ID);
    }

    @Test
    void canceledReservationDoesNotBlockNewReservation() {
        User user = user(AUTHENTICATED_USER_ID);
        ReservationSlot slot = slot(1L, field(1L, "Campo 1"));
        mockSuccessfulReservation(user, slot);

        var response = useCase.execute(AUTHENTICATED_USER_ID, request("slot_1"));

        assertThat(response.id()).isEqualTo("res_123");
        verify(reservationSlotRepository).findFirstAvailableEquivalentSlot(1L, RESERVATION_DATE);
    }

    @Test
    void convertsDatabaseConflictToReservationUnavailable() {
        User user = user(AUTHENTICATED_USER_ID);
        ReservationSlot slot = slot(1L, field(1L, "Campo 1"));
        when(reservationSlotRepository.findById(1L)).thenReturn(Optional.of(slot));
        when(userRepository.findById(AUTHENTICATED_USER_ID)).thenReturn(Optional.of(user));
        when(reservationSlotRepository.findFirstAvailableEquivalentSlot(1L, RESERVATION_DATE))
                .thenReturn(Optional.of(slot));
        when(reservationRepository.save(any(Reservation.class)))
                .thenThrow(new DataIntegrityViolationException("unique constraint"));

        assertThatThrownBy(() -> useCase.execute(AUTHENTICATED_USER_ID, request("slot_1")))
                .isInstanceOf(ReservationUnavailableException.class);
    }

    private void mockSuccessfulReservation(User user, ReservationSlot slot) {
        when(reservationSlotRepository.findById(slot.id())).thenReturn(Optional.of(slot));
        when(userRepository.findById(AUTHENTICATED_USER_ID)).thenReturn(Optional.of(user));
        when(reservationSlotRepository.findFirstAvailableEquivalentSlot(slot.id(), RESERVATION_DATE))
                .thenReturn(Optional.of(slot));
        when(reservationRepository.save(any(Reservation.class)))
                .thenAnswer(invocation -> persisted(invocation.getArgument(0), 123L));
    }

    private CreateReservationRequest request(String slotId) {
        return new CreateReservationRequest(RESERVATION_DATE, slotId);
    }

    private Reservation persisted(Reservation reservation, Long id) {
        return new Reservation(
                id,
                reservation.user(),
                reservation.field(),
                reservation.slot(),
                reservation.reservationDate(),
                reservation.status(),
                reservation.createdAt(),
                reservation.canceledAt()
        );
    }

    private User user(Long id) {
        LocalDateTime now = LocalDateTime.now();
        return new User(
                id,
                "Joao",
                "Silva",
                LocalDate.of(1995, 4, 20),
                "12345678900",
                "joao@email.com",
                "$2a$hash",
                now,
                now
        );
    }

    private ReservationSlot slot(Long id, SoccerField field) {
        LocalDateTime now = LocalDateTime.now();
        return new ReservationSlot(
                id,
                field,
                LocalTime.of(18, 0),
                LocalTime.of(19, 0),
                new BigDecimal("150.00"),
                true,
                now,
                now
        );
    }

    private SoccerField field(Long id, String name) {
        LocalDateTime now = LocalDateTime.now();
        return new SoccerField(id, name, true, now, now);
    }
}
