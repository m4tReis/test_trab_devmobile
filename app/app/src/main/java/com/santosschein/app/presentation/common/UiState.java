package com.santosschein.app.presentation.common;

import androidx.annotation.Nullable;

import com.santosschein.app.core.error.AppError;
import lombok.Getter;

@Getter
public final class UiState<T> {

    public enum Status {
        IDLE,
        LOADING,
        SUCCESS,
        EMPTY,
        ERROR
    }

    private final Status status;
    @Nullable
    private final T data;
    @Nullable
    private final AppError error;
    @Nullable
    private final String message;

    private UiState(Status status, @Nullable T data, @Nullable AppError error, @Nullable String message) {
        this.status = status;
        this.data = data;
        this.error = error;
        this.message = message;
    }

    public static <T> UiState<T> idle() {
        return new UiState<>(Status.IDLE, null, null, null);
    }

    public static <T> UiState<T> loading() {
        return new UiState<>(Status.LOADING, null, null, null);
    }

    public static <T> UiState<T> success(T data) {
        return new UiState<>(Status.SUCCESS, data, null, null);
    }

    public static <T> UiState<T> empty(String message) {
        return new UiState<>(Status.EMPTY, null, null, message);
    }

    public static <T> UiState<T> empty(String message, T data) {
        return new UiState<>(Status.EMPTY, data, null, message);
    }

    public static <T> UiState<T> error(AppError error) {
        return new UiState<>(Status.ERROR, null, error, error != null ? error.getMessage() : null);
    }

    public static <T> UiState<T> error(String message) {
        return new UiState<>(Status.ERROR, null, null, message);
    }

    public boolean isLoading() {
        return status == Status.LOADING;
    }

    public boolean isEmpty() {
        return status == Status.EMPTY;
    }

    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }

    public boolean isError() {
        return status == Status.ERROR;
    }

    public String getErrorMessage() {
        if (message != null) {
            return message;
        }
        if (error != null) {
            return error.getMessage();
        }
        return null;
    }
}
