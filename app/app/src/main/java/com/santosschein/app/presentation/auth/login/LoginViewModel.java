package com.santosschein.app.presentation.auth.login;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.santosschein.app.core.util.Result;
import com.santosschein.app.domain.model.AuthSession;
import com.santosschein.app.domain.usecase.LoginUseCase;
import com.santosschein.app.presentation.common.UiState;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class LoginViewModel extends ViewModel {

    private final LoginUseCase loginUseCase;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final MutableLiveData<UiState<AuthSession>> loginState = new MutableLiveData<>(UiState.idle());

    public LoginViewModel(LoginUseCase loginUseCase) {
        this.loginUseCase = loginUseCase;
    }

    public LiveData<UiState<AuthSession>> getLoginState() {
        return loginState;
    }

    public void login(String email, String password) {
        loginState.setValue(UiState.loading());
        executorService.execute(() -> {
            Result<AuthSession> result = loginUseCase.execute(email, password);
            if (result.isSuccess()) {
                loginState.postValue(UiState.success(result.getData()));
            } else {
                loginState.postValue(UiState.error(result.getError()));
            }
        });
    }

    @Override
    protected void onCleared() {
        executorService.shutdownNow();
    }
}
