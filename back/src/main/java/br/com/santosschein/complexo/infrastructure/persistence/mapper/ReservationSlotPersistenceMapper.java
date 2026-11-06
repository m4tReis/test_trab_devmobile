package br.com.santosschein.complexo.infrastructure.persistence.mapper;

import br.com.santosschein.complexo.domain.model.ReservationSlot;
import br.com.santosschein.complexo.infrastructure.persistence.entity.ReservationSlotEntity;

import java.time.LocalTime;

public class ReservationSlotPersistenceMapper {

    private final SoccerFieldPersistenceMapper soccerFieldMapper = new SoccerFieldPersistenceMapper();

    public ReservationSlot toDomain(ReservationSlotEntity entity) {
        return new ReservationSlot(
                entity.getId(),
                soccerFieldMapper.toDomain(entity.getField()),
                LocalTime.parse(entity.getStartTime()),
                LocalTime.parse(entity.getEndTime()),
                entity.getPrice(),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
