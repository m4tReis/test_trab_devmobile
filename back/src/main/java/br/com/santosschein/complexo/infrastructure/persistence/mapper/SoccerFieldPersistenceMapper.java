package br.com.santosschein.complexo.infrastructure.persistence.mapper;

import br.com.santosschein.complexo.domain.model.SoccerField;
import br.com.santosschein.complexo.infrastructure.persistence.entity.SoccerFieldEntity;

public class SoccerFieldPersistenceMapper {

    public SoccerField toDomain(SoccerFieldEntity entity) {
        return new SoccerField(
                entity.getId(),
                entity.getName(),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
