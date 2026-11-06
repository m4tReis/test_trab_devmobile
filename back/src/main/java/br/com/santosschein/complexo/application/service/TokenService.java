package br.com.santosschein.complexo.application.service;

import java.time.Instant;
import java.util.Optional;

public interface TokenService {

    /**
     * Emite o token da sessao: identificacao do usuario, id da sessao e validade (Parte 3).
     */
    String generateAccessToken(Long userId, String email, String sessionId, Instant issuedAt, Instant expiresAt);

    /**
     * Assinatura valida e token dentro da validade.
     */
    boolean isValid(String token);

    Long extractUserId(String token);

    String extractEmail(String token);

    /**
     * Id da sessao (claim "sid"); nulo em tokens emitidos antes da Parte 3.
     */
    String extractSessionId(String token);

    /**
     * Le os dados de um token com assinatura valida, mesmo que ja esteja vencido (usado no logout).
     * Vazio se o token for invalido ou adulterado.
     */
    Optional<TokenClaims> readClaimsAllowingExpired(String token);
}
