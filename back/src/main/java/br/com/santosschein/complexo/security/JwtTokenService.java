package br.com.santosschein.complexo.security;

import br.com.santosschein.complexo.application.service.TokenClaims;
import br.com.santosschein.complexo.application.service.TokenService;
import br.com.santosschein.complexo.infrastructure.config.AppProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class JwtTokenService implements TokenService {

    private static final String EMAIL_CLAIM = "email";
    private static final String SESSION_ID_CLAIM = "sid";

    private final AppProperties appProperties;

    @Override
    public String generateAccessToken(
            Long userId,
            String email,
            String sessionId,
            Instant issuedAt,
            Instant expiresAt
    ) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim(EMAIL_CLAIM, email)
                .claim(SESSION_ID_CLAIM, sessionId)
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey())
                .compact();
    }

    @Override
    public boolean isValid(String token) {
        try {
            parse(token);
            return true;
        } catch (JwtException | IllegalArgumentException exception) {
            return false;
        }
    }

    @Override
    public Long extractUserId(String token) {
        return Long.valueOf(parse(token).getSubject());
    }

    @Override
    public String extractEmail(String token) {
        return parse(token).get(EMAIL_CLAIM, String.class);
    }

    @Override
    public String extractSessionId(String token) {
        return parse(token).get(SESSION_ID_CLAIM, String.class);
    }

    @Override
    public Optional<TokenClaims> readClaimsAllowingExpired(String token) {
        Claims claims;
        try {
            claims = parse(token);
        } catch (ExpiredJwtException exception) {
            // A assinatura ja foi verificada; o token apenas venceu.
            claims = exception.getClaims();
        } catch (JwtException | IllegalArgumentException exception) {
            return Optional.empty();
        }

        try {
            return Optional.of(new TokenClaims(
                    Long.valueOf(claims.getSubject()),
                    claims.get(EMAIL_CLAIM, String.class),
                    claims.get(SESSION_ID_CLAIM, String.class),
                    claims.getExpiration() != null ? claims.getExpiration().toInstant() : null
            ));
        } catch (RuntimeException exception) {
            return Optional.empty();
        }
    }

    private Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey signingKey() {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(appProperties.jwt().secret().getBytes(StandardCharsets.UTF_8));
            return Keys.hmacShaKeyFor(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("Algoritmo SHA-256 indisponivel.", exception);
        }
    }
}
