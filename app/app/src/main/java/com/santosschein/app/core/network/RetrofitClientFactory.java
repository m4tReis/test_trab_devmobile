package com.santosschein.app.core.network;

import com.santosschein.app.core.config.AppConfig;
import com.santosschein.app.core.session.SessionManager;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class RetrofitClientFactory {

    private RetrofitClientFactory() {
    }

    public static Retrofit create(AppConfig appConfig, SessionManager sessionManager) {
        return new Retrofit.Builder()
                .baseUrl(appConfig.getApiBaseUrl())
                .client(OkHttpClientFactory.create(sessionManager))
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }
}
