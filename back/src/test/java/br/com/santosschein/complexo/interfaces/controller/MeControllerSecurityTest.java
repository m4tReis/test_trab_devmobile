package br.com.santosschein.complexo.interfaces.controller;

import br.com.santosschein.complexo.application.service.TokenService;
import br.com.santosschein.complexo.application.usecase.GetAuthenticatedUserProfileUseCase;
import br.com.santosschein.complexo.application.usecase.auth.ValidateSessionUseCase;
import br.com.santosschein.complexo.interfaces.dto.response.UserProfileResponse;
import br.com.santosschein.complexo.security.AuthenticatedUser;
import br.com.santosschein.complexo.security.JwtAuthenticationFilter;
import br.com.santosschein.complexo.security.SecurityConfig;
import br.com.santosschein.complexo.shared.exception.ApiErrorResponseWriter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MeController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, ApiErrorResponseWriter.class})
class MeControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetAuthenticatedUserProfileUseCase getAuthenticatedUserProfileUseCase;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private ValidateSessionUseCase validateSessionUseCase;

    @Test
    void returnsUnauthorizedWhenSessionWasClosedByLogout() throws Exception {
        when(tokenService.isValid("token-a1")).thenReturn(true);
        when(tokenService.extractUserId("token-a1")).thenReturn(7L);
        when(tokenService.extractSessionId("token-a1")).thenReturn("A1");
        when(validateSessionUseCase.isActive("A1", 7L)).thenReturn(false);

        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer token-a1"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(getAuthenticatedUserProfileUseCase);
    }

    @Test
    void returnsProfileWhenSessionIsOpen() throws Exception {
        when(tokenService.isValid("token-a2")).thenReturn(true);
        when(tokenService.extractUserId("token-a2")).thenReturn(7L);
        when(tokenService.extractSessionId("token-a2")).thenReturn("A2");
        when(tokenService.extractEmail("token-a2")).thenReturn("ana@email.com");
        when(validateSessionUseCase.isActive("A2", 7L)).thenReturn(true);
        when(userDetailsService.loadUserByUsername("ana@email.com"))
                .thenReturn(new AuthenticatedUser(7L, "ana@email.com", "$2a$hash"));
        when(getAuthenticatedUserProfileUseCase.execute(7L)).thenReturn(new UserProfileResponse(
                "7", "Ana", "Silva", LocalDate.of(1995, 4, 20), "12345678900", "ana@email.com"));

        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer token-a2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ana@email.com"));
    }

    @Test
    void returnsUnauthorizedWhenRequestHasNoAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Sessao expirada ou token invalido."))
                .andExpect(jsonPath("$.path").value("/api/v1/users/me"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void returnsUnauthorizedWhenRequestHasInvalidToken() throws Exception {
        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Sessao expirada ou token invalido."))
                .andExpect(jsonPath("$.path").value("/api/v1/users/me"));
    }
}
