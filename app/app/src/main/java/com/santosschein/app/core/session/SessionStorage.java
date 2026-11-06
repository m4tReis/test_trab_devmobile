package com.santosschein.app.core.session;

import androidx.annotation.Nullable;

public interface SessionStorage {

    void saveJwtToken(String token);

    @Nullable
    String getJwtToken();

    void clear();

    boolean hasActiveSession();
}
