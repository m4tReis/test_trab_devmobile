package br.com.santosschein.complexo.interfaces.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

@Component
@Slf4j
public class ApiRequestLoggingFilter extends OncePerRequestFilter {

    private static final String API_PREFIX = "/api/v1";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (!request.getRequestURI().startsWith(API_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        Instant start = Instant.now();
        try {
            filterChain.doFilter(request, response);
        } finally {
            long durationMillis = Duration.between(start, Instant.now()).toMillis();
            log.info(
                    "HTTP {} {} -> {} ({} ms)",
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    durationMillis
            );
        }
    }
}
