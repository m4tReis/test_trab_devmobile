package com.santosschein.app.core.util;

import androidx.annotation.Nullable;

import com.santosschein.app.core.error.AppError;
import lombok.Getter;

@Getter
public final class Result<T> {

    @Nullable
    private final T data;
    @Nullable
    private final AppError error;

    private Result(@Nullable T data, @Nullable AppError error) {
        this.data = data;
        this.error = error;
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(data, null);
    }

    public static <T> Result<T> failure(AppError error) {
        return new Result<>(null, error);
    }

    public boolean isSuccess() {
        return error == null;
    }
}
