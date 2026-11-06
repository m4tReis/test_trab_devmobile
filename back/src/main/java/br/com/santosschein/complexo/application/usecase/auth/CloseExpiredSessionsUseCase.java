package br.com.santosschein.complexo.application.usecase.auth;

import br.com.santosschein.complexo.domain.repository.UserSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Fecha como EXPIRACAO as sessoes vencidas: closed_at recebe a propria validade e logout_at continua nulo.
 * Fechar o app ou perder a conexao nao comprova uma saida, entao a sessao so termina aqui.
 */
@Service
@RequiredArgsConstructor
public class CloseExpiredSessionsUseCase {

    private final UserSessionRepository userSessionRepository;
    private final Clock clock;

    @Transactional
    public int execute() {
        return userSessionRepository.closeExpired(LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC));
    }
}
