package br.com.santosschein.complexo.application.service;

import java.time.Instant;

/**
 * Dados lidos de um token assinado pelo servidor.
 */
public record TokenClaims(
        Long userId,
        String email,
        String sessionId,
        Instant expiresAt
) {
}
