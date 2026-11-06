package br.com.santosschein.complexo.application.usecase.auth;

import br.com.santosschein.complexo.domain.model.SessionCloseReason;
import br.com.santosschein.complexo.domain.model.UserSession;
import br.com.santosschein.complexo.domain.repository.UserSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValidateSessionUseCaseTest {

    @Mock
    private UserSessionRepository userSessionRepository;

    private ValidateSessionUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ValidateSessionUseCase(
                userSessionRepository,
                Clock.fixed(Instant.parse("2026-10-23T22:30:00Z"), ZoneId.of("America/Sao_Paulo"))
        );
    }

    @Test
    void openSessionOfTheSameUserIsActive() {
        when(userSessionRepository.findById("A2")).thenReturn(Optional.of(session("A2", 7L, null, null)));

        assertThat(useCase.isActive("A2", 7L)).isTrue();
    }

    @Test
    void sessionClosedByLogoutIsNotActive() {
        when(userSessionRepository.findById("A1")).thenReturn(Optional.of(
                session("A1", 7L, LocalDateTime.of(2026, 10, 23, 22, 20), SessionCloseReason.LOGOUT)));

        assertThat(useCase.isActive("A1", 7L)).isFalse();
    }

    @Test
    void expiredSessionIsNotActiveEvenBeforeBeingClosed() {
        UserSession expired = new UserSession(
                "A3", 7L, "Ana Silva", null,
                LocalDateTime.of(2026, 10, 23, 21, 0),
                LocalDateTime.of(2026, 10, 23, 22, 0),
                null, null, null
        );
        when(userSessionRepository.findById("A3")).thenReturn(Optional.of(expired));

        assertThat(useCase.isActive("A3", 7L)).isFalse();
    }

    @Test
    void sessionOfAnotherUserIsNotActive() {
        when(userSessionRepository.findById("A2")).thenReturn(Optional.of(session("A2", 7L, null, null)));

        assertThat(useCase.isActive("A2", 8L)).isFalse();
    }

    @Test
    void tokenWithoutSessionIsNotActive() {
        assertThat(useCase.isActive(null, 7L)).isFalse();
        verifyNoInteractions(userSessionRepository);
    }

    private UserSession session(String id, Long userId, LocalDateTime closedAt, SessionCloseReason reason) {
        return new UserSession(
                id, userId, "Ana Silva", "Android",
                LocalDateTime.of(2026, 10, 23, 22, 0),
                LocalDateTime.of(2026, 10, 23, 23, 0),
                reason == SessionCloseReason.LOGOUT ? closedAt : null,
                closedAt,
                reason
        );
    }
}
