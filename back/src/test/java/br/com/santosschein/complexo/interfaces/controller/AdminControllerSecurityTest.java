package br.com.santosschein.complexo.interfaces.controller;

import br.com.santosschein.complexo.application.service.TokenService;
import br.com.santosschein.complexo.application.usecase.admin.ListAccessLogsUseCase;
import br.com.santosschein.complexo.application.usecase.admin.ListUsersForAdminUseCase;
import br.com.santosschein.complexo.application.usecase.auth.ValidateSessionUseCase;
import br.com.santosschein.complexo.domain.model.UserRole;
import br.com.santosschein.complexo.interfaces.dto.response.AccessLogPageResponse;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({AdminController.class, AdminPageController.class})
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, ApiErrorResponseWriter.class})
class AdminControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ListAccessLogsUseCase listAccessLogsUseCase;

    @MockitoBean
    private ListUsersForAdminUseCase listUsersForAdminUseCase;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private ValidateSessionUseCase validateSessionUseCase;

    @Test
    void withoutAuthenticationReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/admin/access-logs"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(listAccessLogsUseCase);
    }

    @Test
    void clientWithoutAdminRoleReturns403EvenCallingTheApiDirectly() throws Exception {
        authenticate("token-cliente", 7L, "S1", "ana@email.com", UserRole.CLIENTE);

        mockMvc.perform(get("/api/v1/admin/access-logs")
                        .header("Authorization", "Bearer token-cliente"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Acesso restrito ao administrador."));

        verifyNoInteractions(listAccessLogsUseCase);
    }

    @Test
    void adminCanQueryTheHistoryWithFilters() throws Exception {
        authenticate("token-admin", 1L, "S9", "admin@email.com", UserRole.ADMIN);
        when(listAccessLogsUseCase.execute(any(), any(), any(), any(), any()))
                .thenReturn(new AccessLogPageResponse(List.of(), 0, 20, 0, 0));

        mockMvc.perform(get("/api/v1/admin/access-logs")
                        .param("from", "2026-10-23")
                        .param("to", "2026-10-24")
                        .param("userId", "7")
                        .header("Authorization", "Bearer token-admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));

        verify(listAccessLogsUseCase).execute(LocalDate.of(2026, 10, 23), LocalDate.of(2026, 10, 24), 7L, 0, 20);
    }

    @Test
    void adminWhoseSessionWasClosedReturns401() throws Exception {
        when(tokenService.isValid("token-admin")).thenReturn(true);
        when(tokenService.extractUserId("token-admin")).thenReturn(1L);
        when(tokenService.extractSessionId("token-admin")).thenReturn("S9");
        when(validateSessionUseCase.isActive("S9", 1L)).thenReturn(false);

        mockMvc.perform(get("/api/v1/admin/users")
                        .header("Authorization", "Bearer token-admin"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(listUsersForAdminUseCase);
    }

    @Test
    void adminPageIsPublicAndRedirectsToStaticFile() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().isFound());
    }

    private void authenticate(String token, Long userId, String sessionId, String email, UserRole role) {
        when(tokenService.isValid(token)).thenReturn(true);
        when(tokenService.extractUserId(token)).thenReturn(userId);
        when(tokenService.extractSessionId(token)).thenReturn(sessionId);
        when(tokenService.extractEmail(token)).thenReturn(email);
        when(validateSessionUseCase.isActive(sessionId, userId)).thenReturn(true);
        when(userDetailsService.loadUserByUsername(email))
                .thenReturn(new AuthenticatedUser(userId, email, "$2a$hash", role, null));
    }
}
