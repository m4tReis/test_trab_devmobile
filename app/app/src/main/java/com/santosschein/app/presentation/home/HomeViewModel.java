package com.santosschein.app.presentation.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.santosschein.app.core.util.Result;
import com.santosschein.app.domain.model.Reservation;
import com.santosschein.app.domain.model.User;
import com.santosschein.app.domain.usecase.GetNextReservationUseCase;
import com.santosschein.app.domain.usecase.GetProfileUseCase;
import com.santosschein.app.presentation.common.UiState;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class HomeViewModel extends ViewModel {

    private final GetProfileUseCase getProfileUseCase;
    private final GetNextReservationUseCase getNextReservationUseCase;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final MutableLiveData<UiState<HomeUiModel>> homeState = new MutableLiveData<>(UiState.idle());

    public HomeViewModel(
            GetProfileUseCase getProfileUseCase,
            GetNextReservationUseCase getNextReservationUseCase
    ) {
        this.getProfileUseCase = getProfileUseCase;
        this.getNextReservationUseCase = getNextReservationUseCase;
    }

    public LiveData<UiState<HomeUiModel>> getHomeState() {
        return homeState;
    }

    public void load() {
        homeState.setValue(UiState.loading());
        executorService.execute(() -> {
            Result<User> profileResult = getProfileUseCase.execute();
            if (!profileResult.isSuccess()) {
                homeState.postValue(UiState.error(profileResult.getError()));
                return;
            }

            Result<Reservation> reservationResult = getNextReservationUseCase.execute();
            if (!reservationResult.isSuccess()) {
                homeState.postValue(UiState.error(reservationResult.getError()));
                return;
            }

            homeState.postValue(UiState.success(new HomeUiModel(
                    profileResult.getData(),
                    reservationResult.getData()
            )));
        });
    }

    @Override
    protected void onCleared() {
        executorService.shutdownNow();
    }
}
