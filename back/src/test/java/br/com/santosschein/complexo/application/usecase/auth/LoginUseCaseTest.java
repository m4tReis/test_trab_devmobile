package br.com.santosschein.complexo.application.usecase.auth;

import br.com.santosschein.complexo.application.service.PasswordService;
import br.com.santosschein.complexo.application.service.TokenService;
import br.com.santosschein.complexo.domain.model.User;
import br.com.santosschein.complexo.domain.model.UserSession;
import br.com.santosschein.complexo.domain.repository.UserRepository;
import br.com.santosschein.complexo.domain.repository.UserSessionRepository;
import br.com.santosschein.complexo.infrastructure.config.AppProperties;
import br.com.santosschein.complexo.interfaces.dto.request.LoginRequest;
import br.com.santosschein.complexo.shared.exception.InvalidCredentialsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginUseCaseTest {

    private static final Instant NOW = Instant.parse("2026-10-23T22:00:00Z");

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordService passwordService;

    @Mock
    private TokenService tokenService;

    @Mock
    private UserSessionRepository userSessionRepository;

    private LoginUseCase useCase;

    @BeforeEach
    void setUp() {
        AppProperties appProperties = new AppProperties(
                new AppProperties.Jwt("test-secret-with-enough-length-for-hmac-signing", 3600),
                new AppProperties.Reservation(60, 24)
        );
        useCase = new LoginUseCase(
                userRepository,
                passwordService,
                tokenService,
                userSessionRepository,
                appProperties,
                Clock.fixed(NOW, ZoneId.of("America/Sao_Paulo"))
        );
    }

    @Test
    void createsAccessLogBeforeIssuingToken() {
        LoginRequest request = new LoginRequest("joao@email.com", "Senha@123");
        User user = user();
        when(userRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(user));
        when(passwordService.matches(request.password(), user.passwordHash())).thenReturn(true);
        when(userSessionRepository.create(any(UserSession.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(tokenService.generateAccessToken(eq(1L), eq("joao@email.com"), anyString(), eq(NOW), eq(NOW.plusSeconds(3600))))
                .thenReturn("jwt-token");

        String token = useCase.execute(request, "Android A");

        assertThat(token).isEqualTo("jwt-token");

        ArgumentCaptor<UserSession> sessionCaptor = ArgumentCaptor.forClass(UserSession.class);
        InOrder order = inOrder(userSessionRepository, tokenService);
        order.verify(userSessionRepository).create(sessionCaptor.capture());
        order.verify(tokenService).generateAccessToken(eq(1L), eq("joao@email.com"), anyString(), eq(NOW), eq(NOW.plusSeconds(3600)));

        UserSession session = sessionCaptor.getValue();
        assertThat(session.id()).isNotBlank();
        assertThat(session.userId()).isEqualTo(1L);
        assertThat(session.userName()).isEqualTo("Joao Silva");
        assertThat(session.device()).isEqualTo("Android A");
        // Instantes gravados em UTC, independentemente do fuso do servidor.
        assertThat(session.loginAt()).isEqualTo(LocalDateTime.of(2026, 10, 23, 22, 0));
        assertThat(session.expiresAt()).isEqualTo(LocalDateTime.of(2026, 10, 23, 23, 0));
        assertThat(session.logoutAt()).isNull();
        assertThat(session.closedAt()).isNull();
        assertThat(session.closeReason()).isNull();

        verify(tokenService).generateAccessToken(1L, "joao@email.com", session.id(), NOW, NOW.plusSeconds(3600));
    }

    @Test
    void eachLoginCreatesItsOwnSession() {
        LoginRequest request = new LoginRequest("joao@email.com", "Senha@123");
        User user = user();
        when(userRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(user));
        when(passwordService.matches(request.password(), user.passwordHash())).thenReturn(true);
        when(userSessionRepository.create(any(UserSession.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(tokenService.generateAccessToken(any(), any(), any(), any(), any())).thenReturn("jwt");

        useCase.execute(request, "Android A");
        useCase.execute(request, "Android B");

        ArgumentCaptor<UserSession> sessionCaptor = ArgumentCaptor.forClass(UserSession.class);
        verify(userSessionRepository, org.mockito.Mockito.times(2)).create(sessionCaptor.capture());
        assertThat(sessionCaptor.getAllValues().get(0).id())
                .isNotEqualTo(sessionCaptor.getAllValues().get(1).id());
    }

    @Test
    void normalizesEmailBeforeSearching() {
        LoginRequest request = new LoginRequest("  Joao@Email.COM ", "Senha@123");
        User user = user();
        when(userRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(user));
        when(passwordService.matches(request.password(), user.passwordHash())).thenReturn(true);
        when(userSessionRepository.create(any(UserSession.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(tokenService.generateAccessToken(any(), any(), any(), any(), any())).thenReturn("jwt");

        assertThat(useCase.execute(request)).isEqualTo("jwt");
    }

    @Test
    void doesNotIssueTokenWhenAccessLogCannotBeSaved() {
        LoginRequest request = new LoginRequest("joao@email.com", "Senha@123");
        User user = user();
        when(userRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(user));
        when(passwordService.matches(request.password(), user.passwordHash())).thenReturn(true);
        when(userSessionRepository.create(any(UserSession.class))).thenThrow(new IllegalStateException("falha no banco"));

        assertThatThrownBy(() -> useCase.execute(request, "Android A"))
                .isInstanceOf(IllegalStateException.class);

        verifyNoInteractions(tokenService);
    }

    @Test
    void rejectsLoginWithInvalidPasswordWithoutCreatingSession() {
        LoginRequest request = new LoginRequest("joao@email.com", "senha-errada");
        User user = user();
        when(userRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(user));
        when(passwordService.matches(request.password(), user.passwordHash())).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(InvalidCredentialsException.class);

        verifyNoInteractions(userSessionRepository, tokenService);
    }

    @Test
    void rejectsLoginWhenUserDoesNotExist() {
        LoginRequest request = new LoginRequest("naoexiste@email.com", "Senha@123");
        when(userRepository.findByEmail("naoexiste@email.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(passwordService, never()).matches(request.password(), "$2a$hash");
        verifyNoInteractions(userSessionRepository, tokenService);
    }

    private User user() {
        LocalDateTime now = LocalDateTime.now();
        return new User(
                1L,
                "Joao",
                "Silva",
                LocalDate.of(1995, 4, 20),
                "12345678900",
                "joao@email.com",
                "$2a$hash",
                now,
                now
        );
    }
}
