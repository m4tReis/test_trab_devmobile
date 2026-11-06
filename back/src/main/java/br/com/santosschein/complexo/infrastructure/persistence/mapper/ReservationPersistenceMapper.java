package br.com.santosschein.complexo.infrastructure.persistence.mapper;

import br.com.santosschein.complexo.domain.model.Reservation;
import br.com.santosschein.complexo.infrastructure.persistence.entity.ReservationEntity;
import br.com.santosschein.complexo.infrastructure.persistence.entity.ReservationSlotEntity;
import br.com.santosschein.complexo.infrastructure.persistence.entity.SoccerFieldEntity;
import br.com.santosschein.complexo.infrastructure.persistence.entity.UserEntity;

public class ReservationPersistenceMapper {

    private final UserPersistenceMapper userMapper = new UserPersistenceMapper();
    private final SoccerFieldPersistenceMapper soccerFieldMapper = new SoccerFieldPersistenceMapper();
    private final ReservationSlotPersistenceMapper reservationSlotMapper = new ReservationSlotPersistenceMapper();

    public Reservation toDomain(ReservationEntity entity) {
        return new Reservation(
                entity.getId(),
                userMapper.toDomain(entity.getUser()),
                soccerFieldMapper.toDomain(entity.getField()),
                reservationSlotMapper.toDomain(entity.getSlot()),
                entity.getReservationDate(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getCanceledAt(),
                entity.getPrice()
        );
    }

    public ReservationEntity toEntity(
            Reservation reservation,
            UserEntity user,
            SoccerFieldEntity field,
            ReservationSlotEntity slot
    ) {
        return new ReservationEntity(
                reservation.id(),
                user,
                field,
                slot,
                reservation.reservationDate(),
                reservation.status(),
                reservation.createdAt(),
                reservation.canceledAt(),
                reservation.chargedPrice()
        );
    }
}
