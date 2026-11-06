package br.com.santosschein.complexo.interfaces.controller;

import br.com.santosschein.complexo.application.usecase.admin.ListAccessLogsUseCase;
import br.com.santosschein.complexo.application.usecase.admin.ListUsersForAdminUseCase;
import br.com.santosschein.complexo.interfaces.dto.response.AccessLogPageResponse;
import br.com.santosschein.complexo.interfaces.dto.response.AdminUserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * Area administrativa (RF12). A permissao ADMIN e verificada no SecurityConfig:
 * sem autenticacao = 401; autenticado sem perfil ADMIN = 403.
 */
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ListAccessLogsUseCase listAccessLogsUseCase;
    private final ListUsersForAdminUseCase listUsersForAdminUseCase;

    @GetMapping("/access-logs")
    public AccessLogPageResponse accessLogs(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size
    ) {
        return listAccessLogsUseCase.execute(from, to, userId, page, size);
    }

    @GetMapping("/users")
    public List<AdminUserResponse> users() {
        return listUsersForAdminUseCase.execute();
    }
}
