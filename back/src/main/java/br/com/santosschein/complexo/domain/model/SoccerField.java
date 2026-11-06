package br.com.santosschein.complexo.domain.model;

import java.time.LocalDateTime;

public record SoccerField(
        Long id,
        String name,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
