package br.com.santosschein.complexo.application.usecase.auth;

import br.com.santosschein.complexo.application.service.TokenClaims;
import br.com.santosschein.complexo.application.service.TokenService;
import br.com.santosschein.complexo.domain.model.SessionCloseReason;
import br.com.santosschein.complexo.domain.model.UserSession;
import br.com.santosschein.complexo.domain.repository.UserSessionRepository;
import br.com.santosschein.complexo.shared.exception.InvalidSessionException;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LogoutUseCaseTest {

    /** 23/10/2026 19:30 em Sao Paulo = 22:30 UTC. */
    private static final Instant NOW = Instant.parse("2026-10-23T22:30:00Z");
    private static final LocalDateTime NOW_UTC = LocalDateTime.of(2026, 10, 23, 22, 30);

    @Mock
    private TokenService tokenService;

    @Mock
    private UserSessionRepository userSessionRepository;

    private LogoutUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new LogoutUseCase(tokenService, userSessionRepository, Clock.fixed(NOW, ZoneId.of("America/Sao_Paulo")));
    }

    @Test
    void closesOnlyTheSessionIdentifiedByTheToken() {
        when(tokenService.readClaimsAllowingExpired("token-a1")).thenReturn(Optional.of(claims("A1")));
        when(userSessionRepository.findById("A1")).thenReturn(Optional.of(openSession("A1")));
        when(userSessionRepository.closeByLogout("A1", NOW_UTC)).thenReturn(true);

        LogoutUseCase.LogoutResult result = useCase.execute("token-a1");

        assertThat(result).isEqualTo(LogoutUseCase.LogoutResult.CLOSED);
        verify(userSessionRepository).closeByLogout("A1", NOW_UTC);
        verify(userSessionRepository, never()).closeByLogout("A2", NOW_UTC);
        verify(userSessionRepository, never()).closeExpired(any());
    }

    @Test
    void repeatingLogoutDoesNotChangeTheLog() {
        UserSession alreadyClosed = new UserSession(
                "A1", 7L, "Ana Silva", "Android A",
                LocalDateTime.of(2026, 10, 23, 22, 0),
                LocalDateTime.of(2026, 10, 23, 23, 0),
                LocalDateTime.of(2026, 10, 23, 22, 30),
                LocalDateTime.of(2026, 10, 23, 22, 30),
                SessionCloseReason.LOGOUT
        );
        when(tokenService.readClaimsAllowingExpired("token-a1")).thenReturn(Optional.of(claims("A1")));
        when(userSessionRepository.findById("A1")).thenReturn(Optional.of(alreadyClosed));

        LogoutUseCase.LogoutResult result = useCase.execute("token-a1");

        assertThat(result).isEqualTo(LogoutUseCase.LogoutResult.ALREADY_CLOSED);
        verify(userSessionRepository, never()).closeByLogout(anyString(), any());
        verify(userSessionRepository, never()).closeExpired(any());
    }

    @Test
    void expiredSessionIsClosedAsExpirationWithoutInventingLogoutTime() {
        when(tokenService.readClaimsAllowingExpired("token-a1")).thenReturn(Optional.of(claims("A1")));
        when(userSessionRepository.findById("A1")).thenReturn(Optional.of(openSession("A1")));
        when(userSessionRepository.closeByLogout("A1", NOW_UTC)).thenReturn(false);

        LogoutUseCase.LogoutResult result = useCase.execute("token-a1");

        assertThat(result).isEqualTo(LogoutUseCase.LogoutResult.EXPIRED);
        verify(userSessionRepository).closeExpired(NOW_UTC);
    }

    @Test
    void ignoresSessionThatBelongsToAnotherUser() {
        when(tokenService.readClaimsAllowingExpired("token-a1")).thenReturn(Optional.of(claims("A1")));
        UserSession otherUsersSession = new UserSession(
                "A1", 99L, "Outra Pessoa", null,
                LocalDateTime.of(2026, 10, 23, 22, 0),
                LocalDateTime.of(2026, 10, 23, 23, 0),
                null, null, null
        );
        when(userSessionRepository.findById("A1")).thenReturn(Optional.of(otherUsersSession));

        assertThat(useCase.execute("token-a1")).isEqualTo(LogoutUseCase.LogoutResult.NOT_FOUND);
        verify(userSessionRepository, never()).closeByLogout(anyString(), any());
    }

    @Test
    void rejectsInvalidOrMissingToken() {
        when(tokenService.readClaimsAllowingExpired("adulterado")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute("adulterado")).isInstanceOf(InvalidSessionException.class);
        assertThatThrownBy(() -> useCase.execute(" ")).isInstanceOf(InvalidSessionException.class);
        assertThatThrownBy(() -> useCase.execute(null)).isInstanceOf(InvalidSessionException.class);
        verifyNoInteractions(userSessionRepository);
    }

    private TokenClaims claims(String sessionId) {
        return new TokenClaims(7L, "ana@email.com", sessionId, Instant.parse("2026-10-23T23:00:00Z"));
    }

    private UserSession openSession(String id) {
        return new UserSession(
                id, 7L, "Ana Silva", "Android A",
                LocalDateTime.of(2026, 10, 23, 22, 0),
                LocalDateTime.of(2026, 10, 23, 23, 0),
                null, null, null
        );
    }
}
