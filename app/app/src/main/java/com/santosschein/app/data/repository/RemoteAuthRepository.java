package com.santosschein.app.data.repository;

import com.santosschein.app.core.error.AppError;
import com.santosschein.app.core.error.ErrorType;
import com.santosschein.app.core.network.HttpErrorMapper;
import com.santosschein.app.core.session.SessionManager;
import com.santosschein.app.core.util.Result;
import com.santosschein.app.data.remote.api.AuthApi;
import com.santosschein.app.data.remote.dto.LoginRequestDto;
import com.santosschein.app.data.remote.mapper.RegisterUserMapper;
import com.santosschein.app.domain.model.AuthSession;
import com.santosschein.app.domain.model.RegisterUserInput;
import com.santosschein.app.domain.repository.AuthRepository;

import java.io.IOException;

import lombok.RequiredArgsConstructor;
import retrofit2.Response;

@RequiredArgsConstructor
public final class RemoteAuthRepository implements AuthRepository {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String REGISTER_CONFLICT_MESSAGE = "E-mail ou documento ja cadastrado.";
    public static final String LOGOUT_NOT_CONFIRMED_MESSAGE =
            "Voce saiu deste aparelho, mas o servidor nao confirmou o encerramento. "
                    + "A sessao sera encerrada automaticamente quando expirar.";

    private final AuthApi authApi;
    private final SessionManager sessionManager;
    private final HttpErrorMapper errorMapper;
    private final RegisterUserMapper registerUserMapper = new RegisterUserMapper();

    @Override
    public Result<AuthSession> login(String email, String password) {
        try {
            Response<Void> response = authApi.login(new LoginRequestDto(email, password)).execute();
            if (!response.isSuccessful()) {
                return Result.failure(errorMapper.fromLoginHttpCode(response.code()));
            }

            String authorizationHeader = response.headers().get(AUTHORIZATION_HEADER);
            if (authorizationHeader == null || authorizationHeader.trim().isEmpty()) {
                return Result.failure(new AppError(
                        ErrorType.UNKNOWN,
                        "Ocorreu um erro inesperado. Tente novamente.",
                        null,
                        null
                ));
            }

            sessionManager.saveAuthorizationHeader(authorizationHeader);
            return Result.success(new AuthSession(sessionManager.getJwtToken(), null));
        } catch (IOException exception) {
            return Result.failure(errorMapper.fromNetworkError(exception));
        } catch (RuntimeException exception) {
            return Result.failure(errorMapper.unexpected(exception));
        }
    }

    @Override
    public Result<Void> register(RegisterUserInput input) {
        try {
            Response<Void> response = authApi.register(registerUserMapper.toDto(input)).execute();
            if (!response.isSuccessful()) {
                return Result.failure(errorMapper.fromHttpCode(response.code(), REGISTER_CONFLICT_MESSAGE));
            }
            return Result.success(null);
        } catch (IOException exception) {
            return Result.failure(errorMapper.fromNetworkError(exception));
        } catch (RuntimeException exception) {
            return Result.failure(errorMapper.unexpected(exception));
        }
    }

    /**
     * Parte 3: pede ao servidor para encerrar esta sessao (preenche o logout no historico).
     * A sessao local e sempre apagada. Sem conexao, o servidor mantem o logout em aberto
     * ate a expiracao, sem inventar um horario de saida, e o usuario e avisado.
     */
    @Override
    public Result<Void> logout() {
        try {
            Response<Void> response = authApi.logout().execute();
            // 401: o servidor ja nao reconhece o token (sessao encerrada ou vencida). Nada a confirmar.
            if (response.isSuccessful() || response.code() == 401) {
                return Result.success(null);
            }
            return Result.failure(new AppError(
                    ErrorType.UNKNOWN,
                    LOGOUT_NOT_CONFIRMED_MESSAGE,
                    String.valueOf(response.code()),
                    null
            ));
        } catch (IOException exception) {
            return Result.failure(new AppError(ErrorType.NETWORK, LOGOUT_NOT_CONFIRMED_MESSAGE, null, exception));
        } catch (RuntimeException exception) {
            return Result.failure(new AppError(ErrorType.UNKNOWN, LOGOUT_NOT_CONFIRMED_MESSAGE, null, exception));
        } finally {
            sessionManager.clearSession();
        }
    }
}
