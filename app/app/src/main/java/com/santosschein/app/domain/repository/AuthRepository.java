package com.santosschein.app.domain.repository;

import com.santosschein.app.core.util.Result;
import com.santosschein.app.domain.model.AuthSession;
import com.santosschein.app.domain.model.RegisterUserInput;

public interface AuthRepository {

    Result<AuthSession> login(String email, String password);

    Result<Void> register(RegisterUserInput input);

    /**
     * Encerra a sessao atual no servidor e sempre limpa a sessao local.
     * Falha (com a sessao local ja limpa) quando o servidor nao confirmou o encerramento.
     */
    Result<Void> logout();
}
