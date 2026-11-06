package com.santosschein.app.core.network;

import com.santosschein.app.core.error.AppError;
import com.santosschein.app.core.error.ErrorType;

import java.io.IOException;
import java.net.SocketTimeoutException;

public final class HttpErrorMapper {

    public static final String INVALID_CREDENTIALS_MESSAGE = "E-mail ou senha invalidos.";
    public static final String SESSION_EXPIRED_MESSAGE = "Sessao expirada. Entre novamente.";

    private static final String DEFAULT_NETWORK_MESSAGE = "Nao foi possivel conectar ao servidor. Tente novamente.";
    private static final String DEFAULT_UNEXPECTED_MESSAGE = "Ocorreu um erro inesperado. Tente novamente.";

    public AppError fromHttpCode(int statusCode) {
        return fromHttpCode(statusCode, null);
    }

    /**
     * Erros do login: 401 significa credenciais incorretas.
     */
    public AppError fromLoginHttpCode(int statusCode) {
        if (statusCode == 401) {
            return new AppError(ErrorType.UNAUTHORIZED, INVALID_CREDENTIALS_MESSAGE, "401", null);
        }
        return fromHttpCode(statusCode);
    }

    /**
     * Erros das demais chamadas: 401 significa que a sessao venceu ou foi encerrada no servidor.
     */
    public AppError fromHttpCode(int statusCode, String conflictMessage) {
        switch (statusCode) {
            case 400:
                return new AppError(ErrorType.VALIDATION, "Dados invalidos. Verifique as informacoes e tente novamente.", "400", null);
            case 401:
                return new AppError(ErrorType.UNAUTHORIZED, SESSION_EXPIRED_MESSAGE, "401", null);
            case 403:
                return new AppError(ErrorType.FORBIDDEN, "Voce nao tem permissao para realizar essa acao.", "403", null);
            case 404:
                return new AppError(ErrorType.NOT_FOUND, "Reserva nao encontrada.", "404", null);
            case 409:
                return new AppError(
                        ErrorType.CONFLICT,
                        conflictMessage != null ? conflictMessage : "Esse horario nao esta mais disponivel.",
                        "409",
                        null
                );
            default:
                return new AppError(ErrorType.UNKNOWN, DEFAULT_UNEXPECTED_MESSAGE, String.valueOf(statusCode), null);
        }
    }

    public AppError fromNetworkError(IOException exception) {
        if (exception instanceof SocketTimeoutException) {
            return new AppError(ErrorType.TIMEOUT, DEFAULT_NETWORK_MESSAGE, null, exception);
        }
        return new AppError(ErrorType.NETWORK, DEFAULT_NETWORK_MESSAGE, null, exception);
    }

    public AppError unexpected(Throwable throwable) {
        return new AppError(ErrorType.UNKNOWN, DEFAULT_UNEXPECTED_MESSAGE, null, throwable);
    }
}
