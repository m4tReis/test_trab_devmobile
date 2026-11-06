package com.santosschein.app.domain.repository;

import com.santosschein.app.core.util.Result;
import com.santosschein.app.domain.model.User;

public interface UserRepository {

    Result<User> getProfile();
}
