package br.com.santosschein.complexo.infrastructure.persistence.repository;

import br.com.santosschein.complexo.domain.model.Reservation;
import br.com.santosschein.complexo.domain.model.ReservationStatus;
import br.com.santosschein.complexo.domain.repository.ReservationRepository;
import br.com.santosschein.complexo.infrastructure.persistence.entity.ReservationSlotEntity;
import br.com.santosschein.complexo.infrastructure.persistence.entity.SoccerFieldEntity;
import br.com.santosschein.complexo.infrastructure.persistence.entity.UserEntity;
import br.com.santosschein.complexo.infrastructure.persistence.mapper.ReservationPersistenceMapper;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ReservationRepositoryAdapter implements ReservationRepository {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final JpaReservationRepository jpaReservationRepository;
    private final EntityManager entityManager;
    private final ReservationPersistenceMapper mapper = new ReservationPersistenceMapper();

    @Override
    public Optional<Reservation> findNextActiveReservation(Long userId, LocalDate currentDate, LocalTime currentTime) {
        return jpaReservationRepository.findNextActiveReservation(
                userId,
                currentDate,
                currentTime.format(TIME_FORMATTER)
        ).map(mapper::toDomain);
    }

    @Override
    public List<Reservation> findFutureReservationsByUser(Long userId, LocalDate currentDate, LocalTime currentTime) {
        return jpaReservationRepository.findFutureReservationsByUser(
                        userId,
                        currentDate,
                        currentTime.format(TIME_FORMATTER)
                )
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Reservation> findPastReservationsByUser(Long userId, LocalDate currentDate, LocalTime currentTime) {
        return jpaReservationRepository.findPastReservationsByUser(
                        userId,
                        currentDate,
                        currentTime.format(TIME_FORMATTER)
                )
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<LocalDate> findAvailableDaysBetween(LocalDate monthStart, LocalDate monthEnd) {
        return jpaReservationRepository.findAvailableDaysBetween(monthStart, monthEnd);
    }

    @Override
    public boolean existsByFieldSlotDateAndStatus(
            Long fieldId,
            Long slotId,
            LocalDate reservationDate,
            ReservationStatus status
    ) {
        return jpaReservationRepository.existsByField_IdAndSlot_IdAndReservationDateAndStatus(
                fieldId,
                slotId,
                reservationDate,
                status
        );
    }

    @Override
    public Optional<Reservation> findById(Long reservationId) {
        return jpaReservationRepository.findById(reservationId).map(mapper::toDomain);
    }

    @Override
    public Optional<Reservation> findByIdAndUserId(Long reservationId, Long userId) {
        return jpaReservationRepository.findByIdAndUser_Id(reservationId, userId).map(mapper::toDomain);
    }

    @Override
    public Reservation save(Reservation reservation) {
        UserEntity user = entityManager.getReference(UserEntity.class, reservation.user().id());
        SoccerFieldEntity field = entityManager.getReference(SoccerFieldEntity.class, reservation.field().id());
        ReservationSlotEntity slot = entityManager.getReference(ReservationSlotEntity.class, reservation.slot().id());

        return mapper.toDomain(jpaReservationRepository.saveAndFlush(mapper.toEntity(reservation, user, field, slot)));
    }
}
