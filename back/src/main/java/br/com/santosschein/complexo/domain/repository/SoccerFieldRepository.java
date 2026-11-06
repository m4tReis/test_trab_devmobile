package br.com.santosschein.complexo.domain.repository;

import br.com.santosschein.complexo.domain.model.SoccerField;

import java.util.Optional;

public interface SoccerFieldRepository {

    Optional<SoccerField> findById(Long id);
}
