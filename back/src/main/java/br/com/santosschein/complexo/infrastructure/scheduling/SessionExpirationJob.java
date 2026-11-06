package br.com.santosschein.complexo.infrastructure.scheduling;

import br.com.santosschein.complexo.application.usecase.auth.CloseExpiredSessionsUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Encerra periodicamente as sessoes vencidas (motivo EXPIRACAO).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SessionExpirationJob {

    private final CloseExpiredSessionsUseCase closeExpiredSessionsUseCase;

    @Scheduled(
            initialDelayString = "${app.session.cleanup-initial-delay-ms:30000}",
            fixedDelayString = "${app.session.cleanup-interval-ms:60000}"
    )
    public void closeExpiredSessions() {
        try {
            int closed = closeExpiredSessionsUseCase.execute();
            if (closed > 0) {
                log.info("{} sessao(oes) encerrada(s) por expiracao.", closed);
            }
        } catch (RuntimeException exception) {
            log.warn("Nao foi possivel encerrar as sessoes vencidas: {}", exception.getMessage());
        }
    }
}
