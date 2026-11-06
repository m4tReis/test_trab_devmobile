package br.com.santosschein.complexo.security;

import br.com.santosschein.complexo.application.service.TokenClaims;
import br.com.santosschein.complexo.infrastructure.config.AppProperties;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenServiceTest {

    private final JwtTokenService tokenService = new JwtTokenService(new AppProperties(
            new AppProperties.Jwt("test-secret-with-enough-length-for-hmac-signing", 3600),
            new AppProperties.Reservation(60, 24)
    ));

    @Test
    void generatesValidJwtWithUserEmailAndSessionClaims() {
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        String token = tokenService.generateAccessToken(123L, "joao@email.com", "sessao-1", now, now.plusSeconds(3600));

        assertThat(token).isNotBlank();
        assertThat(tokenService.isValid(token)).isTrue();
        assertThat(tokenService.extractUserId(token)).isEqualTo(123L);
        assertThat(tokenService.extractEmail(token)).isEqualTo("joao@email.com");
        assertThat(tokenService.extractSessionId(token)).isEqualTo("sessao-1");
    }

    @Test
    void rejectsInvalidJwt() {
        assertThat(tokenService.isValid("invalid.token.value")).isFalse();
        assertThat(tokenService.readClaimsAllowingExpired("invalid.token.value")).isEmpty();
    }

    @Test
    void expiredTokenIsInvalidButItsClaimsCanStillBeReadForLogout() {
        Instant issuedAt = Instant.now().minusSeconds(7200).truncatedTo(ChronoUnit.SECONDS);
        Instant expiresAt = issuedAt.plusSeconds(3600);
        String token = tokenService.generateAccessToken(123L, "joao@email.com", "sessao-1", issuedAt, expiresAt);

        assertThat(tokenService.isValid(token)).isFalse();

        TokenClaims claims = tokenService.readClaimsAllowingExpired(token).orElseThrow();
        assertThat(claims.userId()).isEqualTo(123L);
        assertThat(claims.sessionId()).isEqualTo("sessao-1");
        assertThat(claims.expiresAt()).isEqualTo(expiresAt);
    }

    @Test
    void tokenSignedWithAnotherSecretIsRejected() {
        JwtTokenService otherService = new JwtTokenService(new AppProperties(
                new AppProperties.Jwt("another-secret-with-enough-length-for-hmac-signing", 3600),
                new AppProperties.Reservation(60, 24)
        ));
        Instant now = Instant.now();
        String foreignToken = otherService.generateAccessToken(1L, "x@email.com", "s", now, now.plusSeconds(60));

        assertThat(tokenService.isValid(foreignToken)).isFalse();
        assertThat(tokenService.readClaimsAllowingExpired(foreignToken)).isEmpty();
    }
}
