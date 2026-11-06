package com.santosschein.app.domain.usecase;

import com.santosschein.app.core.util.Result;
import com.santosschein.app.domain.model.User;
import com.santosschein.app.domain.repository.UserRepository;

public final class GetProfileUseCase {

    private final UserRepository userRepository;

    public GetProfileUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Result<User> execute() {
        return userRepository.getProfile();
    }
}
