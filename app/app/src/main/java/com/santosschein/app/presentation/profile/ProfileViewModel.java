package com.santosschein.app.presentation.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.santosschein.app.core.util.Result;
import com.santosschein.app.domain.model.User;
import com.santosschein.app.domain.usecase.GetProfileUseCase;
import com.santosschein.app.domain.usecase.LogoutUseCase;
import com.santosschein.app.presentation.common.UiState;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ProfileViewModel extends ViewModel {

    private final GetProfileUseCase getProfileUseCase;
    private final LogoutUseCase logoutUseCase;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final MutableLiveData<UiState<User>> profileState = new MutableLiveData<>(UiState.idle());
    private final MutableLiveData<UiState<Boolean>> logoutState = new MutableLiveData<>(UiState.idle());

    public ProfileViewModel(GetProfileUseCase getProfileUseCase, LogoutUseCase logoutUseCase) {
        this.getProfileUseCase = getProfileUseCase;
        this.logoutUseCase = logoutUseCase;
    }

    public LiveData<UiState<User>> getProfileState() {
        return profileState;
    }

    public void load() {
        profileState.setValue(UiState.loading());
        executorService.execute(() -> {
            Result<User> result = getProfileUseCase.execute();
            if (result.isSuccess()) {
                profileState.postValue(UiState.success(result.getData()));
            } else {
                profileState.postValue(UiState.error(result.getError()));
            }
        });
    }

    public LiveData<UiState<Boolean>> getLogoutState() {
        return logoutState;
    }

    /**
     * Encerra a sessao no servidor fora da thread principal.
     * Sucesso: saida confirmada. Erro: saiu so deste aparelho (sem conexao), com aviso ao usuario.
     */
    public void logout() {
        UiState<Boolean> current = logoutState.getValue();
        if (current != null && current.isLoading()) {
            return;
        }
        logoutState.setValue(UiState.loading());
        executorService.execute(() -> {
            Result<Void> result = logoutUseCase.execute();
            if (result.isSuccess()) {
                logoutState.postValue(UiState.success(true));
            } else {
                logoutState.postValue(UiState.error(result.getError()));
            }
        });
    }

    @Override
    protected void onCleared() {
        executorService.shutdownNow();
    }
}
