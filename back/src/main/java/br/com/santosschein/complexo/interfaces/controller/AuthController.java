package br.com.santosschein.complexo.interfaces.controller;

import br.com.santosschein.complexo.application.usecase.auth.LoginUseCase;
import br.com.santosschein.complexo.application.usecase.auth.LogoutUseCase;
import br.com.santosschein.complexo.application.usecase.auth.RegisterUserUseCase;
import br.com.santosschein.complexo.interfaces.dto.request.LoginRequest;
import br.com.santosschein.complexo.interfaces.dto.request.RegisterUserRequest;
import br.com.santosschein.complexo.shared.exception.InvalidSessionException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    public static final String DEVICE_HEADER = "X-Device-Name";
    private static final String BEARER_PREFIX = "Bearer ";

    private final LoginUseCase loginUseCase;
    private final RegisterUserUseCase registerUserUseCase;
    private final LogoutUseCase logoutUseCase;

    @PostMapping("/login")
    public ResponseEntity<Void> login(
            @Valid @RequestBody LoginRequest request,
            @RequestHeader(value = DEVICE_HEADER, required = false) String device
    ) {
        String token = loginUseCase.execute(request, device);

        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + token)
                .build();
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterUserRequest request) {
        registerUserUseCase.execute(request);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Encerra a sessao atual no servidor (RF09/RF11). Idempotente: repetir devolve 204 sem alterar o log.
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader
    ) {
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            throw new InvalidSessionException();
        }

        logoutUseCase.execute(authorizationHeader.substring(BEARER_PREFIX.length()));
        return ResponseEntity.noContent().build();
    }
}
