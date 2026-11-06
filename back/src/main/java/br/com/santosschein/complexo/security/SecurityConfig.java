package br.com.santosschein.complexo.security;

import br.com.santosschein.complexo.shared.exception.ApiErrorResponseWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String ADMIN_API = "/api/v1/admin";

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ApiErrorResponseWriter apiErrorResponseWriter;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) ->
                                apiErrorResponseWriter.write(
                                        response,
                                        HttpStatus.UNAUTHORIZED,
                                        "Sessao expirada ou token invalido.",
                                        request.getRequestURI()
                                )
                        )
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                apiErrorResponseWriter.write(
                                        response,
                                        HttpStatus.FORBIDDEN,
                                        request.getRequestURI().startsWith(ADMIN_API)
                                                ? "Acesso restrito ao administrador."
                                                : "Sem permissao.",
                                        request.getRequestURI()
                                )
                        )
                )
                .authorizeHttpRequests(authorize -> authorize
                        // Logout e publico de proposito: o caso de uso le o token (mesmo vencido) e
                        // responde 204 de forma idempotente, sem depender da sessao ainda estar aberta.
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/auth/login",
                                "/api/v1/auth/register",
                                "/api/v1/auth/logout"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/health").permitAll()
                        .requestMatchers("/error").permitAll()
                        // Pagina estatica do administrador; os dados vem da API protegida abaixo.
                        .requestMatchers(HttpMethod.GET, "/admin", "/admin/", "/admin/**").permitAll()
                        .requestMatchers(ADMIN_API + "/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
