package com.santosschein.app.core.error;

import androidx.annotation.Nullable;
import lombok.Getter;

@Getter
public final class AppError {

    private final ErrorType type;
    private final String message;
    private final String code;
    @Nullable
    private final Throwable cause;

    public AppError(ErrorType type, String message) {
        this(type, message, null, null);
    }

    public AppError(ErrorType type, String message, @Nullable String code, @Nullable Throwable cause) {
        this.type = type;
        this.message = message;
        this.code = code;
        this.cause = cause;
    }
}
