package com.santosschein.app.presentation.reservation.list;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.santosschein.app.core.util.Result;
import com.santosschein.app.domain.model.Reservation;
import com.santosschein.app.domain.usecase.CancelReservationUseCase;
import com.santosschein.app.domain.usecase.GetPastReservationsUseCase;
import com.santosschein.app.domain.usecase.GetUpcomingReservationsUseCase;
import com.santosschein.app.presentation.common.UiState;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ReservationListViewModel extends ViewModel {

    private final GetUpcomingReservationsUseCase getUpcomingReservationsUseCase;
    private final GetPastReservationsUseCase getPastReservationsUseCase;
    private final CancelReservationUseCase cancelReservationUseCase;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final MutableLiveData<UiState<List<Reservation>>> reservationsState = new MutableLiveData<>(UiState.idle());
    private final MutableLiveData<UiState<Void>> cancelState = new MutableLiveData<>(UiState.idle());

    private boolean showingPast;

    public ReservationListViewModel(
            GetUpcomingReservationsUseCase getUpcomingReservationsUseCase,
            GetPastReservationsUseCase getPastReservationsUseCase,
            CancelReservationUseCase cancelReservationUseCase
    ) {
        this.getUpcomingReservationsUseCase = getUpcomingReservationsUseCase;
        this.getPastReservationsUseCase = getPastReservationsUseCase;
        this.cancelReservationUseCase = cancelReservationUseCase;
    }

    public LiveData<UiState<List<Reservation>>> getReservationsState() {
        return reservationsState;
    }

    public LiveData<UiState<Void>> getCancelState() {
        return cancelState;
    }

    public void load(boolean past) {
        showingPast = past;
        reservationsState.setValue(UiState.loading());
        executorService.execute(() -> {
            Result<List<Reservation>> result = loadCurrentMode();
            postReservationResult(result, past);
        });
    }

    public void cancel(String reservationId) {
        cancelState.setValue(UiState.loading());
        executorService.execute(() -> {
            Result<Void> result = cancelReservationUseCase.execute(reservationId);
            if (result.isSuccess()) {
                cancelState.postValue(UiState.success(null));
                reservationsState.postValue(UiState.loading());
                postReservationResult(loadCurrentMode(), showingPast);
            } else {
                cancelState.postValue(UiState.error(result.getError()));
            }
        });
    }

    private Result<List<Reservation>> loadCurrentMode() {
        return showingPast
                ? getPastReservationsUseCase.execute()
                : getUpcomingReservationsUseCase.execute();
    }

    private void postReservationResult(Result<List<Reservation>> result, boolean past) {
        if (result.isSuccess()) {
            List<Reservation> reservations = result.getData();
            if (reservations == null || reservations.isEmpty()) {
                reservationsState.postValue(UiState.empty(past
                        ? "Nenhuma reserva passada encontrada."
                        : "Nenhuma reserva futura encontrada."));
            } else {
                reservationsState.postValue(UiState.success(reservations));
            }
        } else {
            reservationsState.postValue(UiState.error(result.getError()));
        }
    }

    @Override
    protected void onCleared() {
        executorService.shutdownNow();
    }
}
