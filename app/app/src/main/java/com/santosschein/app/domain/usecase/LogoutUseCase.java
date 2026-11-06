package com.santosschein.app.domain.usecase;

import com.santosschein.app.core.util.Result;
import com.santosschein.app.domain.repository.AuthRepository;

public final class LogoutUseCase {

    private final AuthRepository authRepository;

    public LogoutUseCase(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    public Result<Void> execute() {
        return authRepository.logout();
    }
}
