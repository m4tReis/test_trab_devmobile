package com.santosschein.app.data.remote.api;

import lombok.RequiredArgsConstructor;
import retrofit2.Retrofit;

@RequiredArgsConstructor
public final class ApiServiceFactory {

    private final Retrofit retrofit;

    public ReservationApi createReservationApi() {
        return retrofit.create(ReservationApi.class);
    }

    public AuthApi createAuthApi() {
        return retrofit.create(AuthApi.class);
    }

    public UserApi createUserApi() {
        return retrofit.create(UserApi.class);
    }
}
