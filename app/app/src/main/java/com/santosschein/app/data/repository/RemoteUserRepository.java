package com.santosschein.app.data.repository;

import com.santosschein.app.core.network.HttpErrorMapper;
import com.santosschein.app.core.session.SessionManager;
import com.santosschein.app.core.util.Result;
import com.santosschein.app.data.remote.api.UserApi;
import com.santosschein.app.data.remote.dto.UserProfileDto;
import com.santosschein.app.data.remote.mapper.UserMapper;
import com.santosschein.app.domain.model.User;
import com.santosschein.app.domain.repository.UserRepository;

import java.io.IOException;

import lombok.RequiredArgsConstructor;
import retrofit2.Response;

@RequiredArgsConstructor
public final class RemoteUserRepository implements UserRepository {

    private final UserApi userApi;
    private final SessionManager sessionManager;
    private final HttpErrorMapper errorMapper;
    private final UserMapper userMapper = new UserMapper();

    @Override
    public Result<User> getProfile() {
        try {
            Response<UserProfileDto> response = userApi.getProfile().execute();
            if (!response.isSuccessful() || response.body() == null) {
                if (response.code() == 401) {
                    sessionManager.clearSession();
                }
                return Result.failure(errorMapper.fromHttpCode(response.code()));
            }
            return Result.success(userMapper.toDomain(response.body()));
        } catch (IOException exception) {
            return Result.failure(errorMapper.fromNetworkError(exception));
        } catch (RuntimeException exception) {
            return Result.failure(errorMapper.unexpected(exception));
        }
    }
}
