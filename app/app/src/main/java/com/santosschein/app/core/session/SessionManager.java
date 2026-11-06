package com.santosschein.app.core.session;

import java.util.concurrent.atomic.AtomicBoolean;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class SessionManager {

    private static final String BEARER_PREFIX = "Bearer ";

    private final SessionStorage sessionStorage;

    /**
     * Marca que a sessao foi recusada pelo servidor (401), para a tela de login avisar
     * "Sessao expirada. Entre novamente." em vez de abrir sem explicacao.
     */
    private final AtomicBoolean sessionExpiredNoticePending = new AtomicBoolean(false);

    public void saveAuthorizationHeader(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.trim().isEmpty()) {
            return;
        }

        String token = authorizationHeader.trim();
        if (token.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
            token = token.substring(BEARER_PREFIX.length()).trim();
        }
        saveJwtToken(token);
    }

    public void saveJwtToken(String token) {
        if (token != null && !token.trim().isEmpty()) {
            sessionExpiredNoticePending.set(false);
            sessionStorage.saveJwtToken(token.trim());
        }
    }

    public String getJwtToken() {
        return sessionStorage.getJwtToken();
    }

    public boolean hasActiveSession() {
        return sessionStorage.hasActiveSession();
    }

    public void clearSession() {
        sessionStorage.clear();
    }

    /**
     * O servidor recusou o token salvo (sessao vencida ou encerrada em outro lugar).
     */
    public void markSessionExpired() {
        boolean hadSession = sessionStorage.hasActiveSession();
        sessionStorage.clear();
        if (hadSession) {
            sessionExpiredNoticePending.set(true);
        }
    }

    /**
     * @return true uma unica vez depois de {@link #markSessionExpired()}.
     */
    public boolean consumeSessionExpiredNotice() {
        return sessionExpiredNoticePending.getAndSet(false);
    }
}
