package com.santosschein.app.core.network;

import androidx.annotation.NonNull;

import com.santosschein.app.core.session.SessionManager;

import java.io.IOException;

import lombok.RequiredArgsConstructor;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

@RequiredArgsConstructor
public final class AuthInterceptor implements Interceptor {

    private static final String LOGIN_PATH = "/api/v1/auth/login";
    private static final String REGISTER_PATH = "/api/v1/auth/register";
    private static final String LOGOUT_PATH = "/api/v1/auth/logout";

    private final SessionManager sessionManager;

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request originalRequest = chain.request();
        String path = originalRequest.url().encodedPath();

        // Login e cadastro nunca levam o token antigo.
        if (path.endsWith(LOGIN_PATH) || path.endsWith(REGISTER_PATH)) {
            return chain.proceed(originalRequest);
        }

        String token = sessionManager.getJwtToken();
        if (token == null || token.trim().isEmpty()) {
            return chain.proceed(originalRequest);
        }

        Request authenticatedRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer " + token)
                .build();
        Response response = chain.proceed(authenticatedRequest);

        // O servidor valida a sessao a cada chamada: 401 significa sessao vencida ou encerrada.
        // O proprio logout trata o 401 dele, sem aviso de sessao expirada.
        if (response.code() == 401 && !path.endsWith(LOGOUT_PATH)) {
            sessionManager.markSessionExpired();
        }
        return response;
    }
}
