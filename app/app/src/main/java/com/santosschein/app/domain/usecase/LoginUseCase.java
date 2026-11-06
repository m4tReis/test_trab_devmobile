package com.santosschein.app.domain.usecase;

import com.santosschein.app.core.util.Result;
import com.santosschein.app.domain.model.AuthSession;
import com.santosschein.app.domain.repository.AuthRepository;

public final class LoginUseCase {

    private final AuthRepository authRepository;

    public LoginUseCase(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    public Result<AuthSession> execute(String email, String password) {
        if (ValidationUtils.isInvalidEmail(email)) {
            return ValidationUtils.validationError("Informe um e-mail valido.");
        }
        if (ValidationUtils.isBlank(password)) {
            return ValidationUtils.validationError("Informe a senha.");
        }
        return authRepository.login(email.trim(), password);
    }
}
