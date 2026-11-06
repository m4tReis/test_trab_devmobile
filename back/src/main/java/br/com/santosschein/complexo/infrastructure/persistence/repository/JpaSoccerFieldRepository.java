package br.com.santosschein.complexo.infrastructure.persistence.repository;

import br.com.santosschein.complexo.infrastructure.persistence.entity.SoccerFieldEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaSoccerFieldRepository extends JpaRepository<SoccerFieldEntity, Long> {
}
