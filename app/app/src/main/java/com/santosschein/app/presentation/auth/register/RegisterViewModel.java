package com.santosschein.app.presentation.auth.register;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.santosschein.app.core.util.Result;
import com.santosschein.app.domain.model.RegisterUserInput;
import com.santosschein.app.domain.usecase.RegisterUserUseCase;
import com.santosschein.app.presentation.common.PresentationFormatters;
import com.santosschein.app.presentation.common.UiState;

import java.time.LocalDate;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class RegisterViewModel extends ViewModel {

    private final RegisterUserUseCase registerUserUseCase;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final MutableLiveData<UiState<Void>> registerState = new MutableLiveData<>(UiState.idle());

    public RegisterViewModel(RegisterUserUseCase registerUserUseCase) {
        this.registerUserUseCase = registerUserUseCase;
    }

    public LiveData<UiState<Void>> getRegisterState() {
        return registerState;
    }

    public void register(
            String firstName,
            String lastName,
            String birthDate,
            String document,
            String email,
            String password
    ) {
        LocalDate parsedBirthDate;
        parsedBirthDate = PresentationFormatters.parseBrazilianDate(birthDate);
        if (parsedBirthDate == null) {
            registerState.setValue(UiState.error("Informe a data de nascimento no formato DD/MM/AAAA."));
            return;
        }

        registerState.setValue(UiState.loading());
        RegisterUserInput input = new RegisterUserInput(
                firstName,
                lastName,
                parsedBirthDate,
                PresentationFormatters.onlyDigits(document),
                email,
                password
        );
        executorService.execute(() -> {
            Result<Void> result = registerUserUseCase.execute(input);
            if (result.isSuccess()) {
                registerState.postValue(UiState.success(null));
            } else {
                registerState.postValue(UiState.error(result.getError()));
            }
        });
    }

    @Override
    protected void onCleared() {
        executorService.shutdownNow();
    }
}
