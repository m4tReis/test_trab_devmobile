package com.santosschein.app.data.mock;

import com.santosschein.app.core.util.Result;
import com.santosschein.app.domain.model.User;
import com.santosschein.app.domain.repository.UserRepository;

public final class MockUserRepository implements UserRepository {

    private final MockDataStore dataStore;

    public MockUserRepository(MockDataStore dataStore) {
        this.dataStore = dataStore;
    }

    @Override
    public Result<User> getProfile() {
        return Result.success(dataStore.getDefaultUser());
    }
}
