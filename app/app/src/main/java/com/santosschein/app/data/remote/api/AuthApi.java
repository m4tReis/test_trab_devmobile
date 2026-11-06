package com.santosschein.app.data.remote.api;

import com.santosschein.app.data.remote.dto.LoginRequestDto;
import com.santosschein.app.data.remote.dto.RegisterUserRequestDto;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApi {

    @POST("api/v1/auth/login")
    Call<Void> login(@Body LoginRequestDto request);

    @POST("api/v1/auth/register")
    Call<Void> register(@Body RegisterUserRequestDto request);

    /**
     * Encerra no servidor a sessao do token enviado no cabecalho Authorization (204, idempotente).
     */
    @POST("api/v1/auth/logout")
    Call<Void> logout();
}
