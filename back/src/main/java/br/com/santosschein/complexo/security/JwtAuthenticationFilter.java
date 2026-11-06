package br.com.santosschein.complexo.security;

import br.com.santosschein.complexo.application.service.TokenService;
import br.com.santosschein.complexo.application.usecase.auth.ValidateSessionUseCase;
import br.com.santosschein.complexo.shared.exception.ApiErrorResponseWriter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final TokenService tokenService;
    private final UserDetailsService userDetailsService;
    private final ValidateSessionUseCase validateSessionUseCase;
    private final ApiErrorResponseWriter apiErrorResponseWriter;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorizationHeader.substring(BEARER_PREFIX.length());

        try {
            if (tokenService.isValid(token) && SecurityContextHolder.getContext().getAuthentication() == null) {
                Long userId = tokenService.extractUserId(token);
                String sessionId = tokenService.extractSessionId(token);

                // Parte 3: o token so vale enquanto a sessao estiver aberta no servidor.
                // Sessao encerrada (logout) ou vencida: a requisicao segue sem autenticacao
                // e as rotas protegidas respondem 401.
                if (validateSessionUseCase.isActive(sessionId, userId)) {
                    authenticateRequest(request, token, userId, sessionId);
                }
            }
        } catch (RuntimeException exception) {
            apiErrorResponseWriter.write(
                    response,
                    HttpStatus.UNAUTHORIZED,
                    "Sessao expirada ou token invalido.",
                    request.getRequestURI()
            );
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void authenticateRequest(HttpServletRequest request, String token, Long userId, String sessionId) {
        String email = tokenService.extractEmail(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        if (!(userDetails instanceof AuthenticatedUser authenticatedUser) || !authenticatedUser.id().equals(userId)) {
            throw new IllegalArgumentException("Token invalido para o usuario informado.");
        }

        AuthenticatedUser principal = authenticatedUser.withSessionId(sessionId);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                principal.getAuthorities()
        );
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
