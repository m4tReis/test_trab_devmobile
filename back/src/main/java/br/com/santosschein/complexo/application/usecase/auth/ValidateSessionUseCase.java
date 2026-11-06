package br.com.santosschein.complexo.application.usecase.auth;

import br.com.santosschein.complexo.domain.repository.UserSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Usado em toda requisicao autenticada: o token so vale se a sessao correspondente
 * existir, pertencer ao usuario, nao tiver sido encerrada e estiver dentro da validade.
 */
@Service
@RequiredArgsConstructor
public class ValidateSessionUseCase {

    private final UserSessionRepository userSessionRepository;
    private final Clock clock;

    public boolean isActive(String sessionId, Long userId) {
        if (sessionId == null || userId == null) {
            return false;
        }
        LocalDateTime nowUtc = LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
        return userSessionRepository.findById(sessionId)
                .filter(session -> session.userId().equals(userId))
                .filter(session -> session.isOpenAt(nowUtc))
                .isPresent();
    }
}
