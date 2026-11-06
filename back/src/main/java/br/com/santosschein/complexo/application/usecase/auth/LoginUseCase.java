package br.com.santosschein.complexo.application.usecase.auth;

import br.com.santosschein.complexo.application.service.PasswordService;
import br.com.santosschein.complexo.application.service.TokenService;
import br.com.santosschein.complexo.application.service.UserIdentityNormalizer;
import br.com.santosschein.complexo.domain.model.User;
import br.com.santosschein.complexo.domain.model.UserSession;
import br.com.santosschein.complexo.domain.repository.UserRepository;
import br.com.santosschein.complexo.domain.repository.UserSessionRepository;
import br.com.santosschein.complexo.infrastructure.config.AppProperties;
import br.com.santosschein.complexo.interfaces.dto.request.LoginRequest;
import br.com.santosschein.complexo.shared.exception.InvalidCredentialsException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoginUseCase {

    private static final int MAX_DEVICE_LENGTH = 120;

    private final UserRepository userRepository;
    private final PasswordService passwordService;
    private final TokenService tokenService;
    private final UserSessionRepository userSessionRepository;
    private final AppProperties appProperties;
    private final Clock clock;

    public String execute(LoginRequest request) {
        return execute(request, null);
    }

    /**
     * RF11: cada login bem-sucedido cria um registro de acesso ANTES de o token ser emitido.
     * Se a gravacao falhar, a transacao e desfeita e nenhum token e devolvido.
     *
     * @param device referencia opcional do aparelho (ex.: "Android A"); nao substitui e-mail e senha.
     */
    @Transactional
    public String execute(LoginRequest request, String device) {
        User user = userRepository.findByEmail(UserIdentityNormalizer.email(request.email()))
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordService.matches(request.password(), user.passwordHash())) {
            throw new InvalidCredentialsException();
        }

        Instant now = clock.instant();
        Instant expiresAt = now.plusSeconds(appProperties.jwt().expirationSeconds());

        UserSession session = userSessionRepository.create(new UserSession(
                UUID.randomUUID().toString(),
                user.id(),
                user.fullName(),
                sanitizeDevice(device),
                toUtc(now),
                toUtc(expiresAt),
                null,
                null,
                null
        ));

        return tokenService.generateAccessToken(user.id(), user.email(), session.id(), now, expiresAt);
    }

    private static LocalDateTime toUtc(Instant instant) {
        return LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

    private static String sanitizeDevice(String device) {
        if (device == null || device.isBlank()) {
            return null;
        }
        String trimmed = device.trim();
        return trimmed.length() > MAX_DEVICE_LENGTH ? trimmed.substring(0, MAX_DEVICE_LENGTH) : trimmed;
    }
}
