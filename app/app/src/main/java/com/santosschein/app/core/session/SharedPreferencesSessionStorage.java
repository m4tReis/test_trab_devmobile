package com.santosschein.app.core.session;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

public final class SharedPreferencesSessionStorage implements SessionStorage {

    private static final String PREF_NAME = "santos_schein_session";
    private static final String KEY_JWT_TOKEN = "jwt_token";

    private final SharedPreferences preferences;

    public SharedPreferencesSessionStorage(Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public void saveJwtToken(String token) {
        preferences.edit().putString(KEY_JWT_TOKEN, token).apply();
    }

    @Nullable
    @Override
    public String getJwtToken() {
        return preferences.getString(KEY_JWT_TOKEN, null);
    }

    @Override
    public void clear() {
        preferences.edit().clear().apply();
    }

    @Override
    public boolean hasActiveSession() {
        String token = getJwtToken();
        return token != null && !token.trim().isEmpty();
    }
}
