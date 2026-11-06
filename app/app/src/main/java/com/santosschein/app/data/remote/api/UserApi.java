package com.santosschein.app.data.remote.api;

import com.santosschein.app.data.remote.dto.UserProfileDto;

import retrofit2.Call;
import retrofit2.http.GET;

public interface UserApi {

    @GET("api/v1/users/me")
    Call<UserProfileDto> getProfile();
}
