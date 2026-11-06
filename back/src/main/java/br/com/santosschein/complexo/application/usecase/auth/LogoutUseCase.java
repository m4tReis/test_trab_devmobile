package br.com.santosschein.complexo.application.usecase.auth;

import br.com.santosschein.complexo.application.service.TokenClaims;
import br.com.santosschein.complexo.application.service.TokenService;
import br.com.santosschein.complexo.domain.model.UserSession;
import br.com.santosschein.complexo.domain.repository.UserSessionRepository;
import br.com.santosschein.complexo.shared.exception.InvalidSessionException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

/**
 * RF09/RF11: encerra no servidor somente a sessao identificada pelo token.
 * <ul>
 *     <li>Sessao aberta: grava logout_at/closed_at com o mesmo instante e motivo LOGOUT, em um unico UPDATE
 *     (o registro e a revogacao acontecem juntos).</li>
 *     <li>Sessao ja encerrada: nada muda (logout idempotente).</li>
 *     <li>Sessao vencida: encerrada como EXPIRACAO, sem inventar um horario de logout.</li>
 * </ul>
 * Outras sessoes da mesma conta nao sao afetadas.
 */
@Service
@RequiredArgsConstructor
public class LogoutUseCase {

    private final TokenService tokenService;
    private final UserSessionRepository userSessionRepository;
    private final Clock clock;

    @Transactional
    public LogoutResult execute(String token) {
        TokenClaims claims = Optional.ofNullable(token)
                .filter(value -> !value.isBlank())
                .flatMap(tokenService::readClaimsAllowingExpired)
                .orElseThrow(InvalidSessionException::new);

        if (claims.sessionId() == null) {
            // Token antigo, emitido antes do registro de sessoes: nao ha o que encerrar no servidor.
            return LogoutResult.NOT_FOUND;
        }

        Optional<UserSession> session = userSessionRepository.findById(claims.sessionId())
                .filter(found -> found.userId().equals(claims.userId()));
        if (session.isEmpty()) {
            return LogoutResult.NOT_FOUND;
        }

        if (session.get().isClosed()) {
            return LogoutResult.ALREADY_CLOSED;
        }

        LocalDateTime nowUtc = LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
        if (userSessionRepository.closeByLogout(claims.sessionId(), nowUtc)) {
            return LogoutResult.CLOSED;
        }

        // Nao fechou por logout: a sessao venceu (ou foi encerrada em paralelo).
        userSessionRepository.closeExpired(nowUtc);
        return LogoutResult.EXPIRED;
    }

    public enum LogoutResult {
        CLOSED,
        ALREADY_CLOSED,
        EXPIRED,
        NOT_FOUND
    }
}
