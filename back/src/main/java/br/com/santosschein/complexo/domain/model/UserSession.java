package br.com.santosschein.complexo.domain.model;

import java.time.LocalDateTime;

/**
 * Um acesso do usuario (entidade acesso_usuario da Parte 3): cada login cria uma sessao propria.
 * Todos os instantes estao em UTC.
 *
 * @param logoutAt    preenchido apenas em logout confirmado; permanece nulo na expiracao.
 * @param closedAt    instante de encerramento (logout ou expiracao); nulo enquanto a sessao esta aberta.
 * @param closeReason LOGOUT ou EXPIRACAO; nulo enquanto a sessao esta aberta.
 */
public record UserSession(
        String id,
        Long userId,
        String userName,
        String device,
        LocalDateTime loginAt,
        LocalDateTime expiresAt,
        LocalDateTime logoutAt,
        LocalDateTime closedAt,
        SessionCloseReason closeReason
) {

    public boolean isClosed() {
        return closedAt != null;
    }

    /**
     * Sessao valida: nao encerrada e ainda dentro da validade.
     */
    public boolean isOpenAt(LocalDateTime nowUtc) {
        return !isClosed() && expiresAt.isAfter(nowUtc);
    }

    /**
     * Situacao exibida ao administrador.
     */
    public SessionStatus statusAt(LocalDateTime nowUtc) {
        if (closeReason == SessionCloseReason.LOGOUT) {
            return SessionStatus.LOGOUT;
        }
        if (closeReason == SessionCloseReason.EXPIRACAO || !expiresAt.isAfter(nowUtc)) {
            return SessionStatus.EXPIRADA;
        }
        return SessionStatus.EM_ABERTO;
    }

    public enum SessionStatus {
        EM_ABERTO,
        LOGOUT,
        EXPIRADA
    }
}
