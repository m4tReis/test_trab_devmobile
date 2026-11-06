package com.santosschein.app.presentation.reservation.create;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.santosschein.app.core.util.Result;
import com.santosschein.app.domain.model.Reservation;
import com.santosschein.app.domain.model.ReservationSlot;
import com.santosschein.app.domain.usecase.CreateReservationUseCase;
import com.santosschein.app.domain.usecase.GetAvailableDaysUseCase;
import com.santosschein.app.domain.usecase.GetAvailableSlotsUseCase;
import com.santosschein.app.presentation.common.UiState;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class CreateReservationViewModel extends ViewModel {

    private final GetAvailableDaysUseCase getAvailableDaysUseCase;
    private final GetAvailableSlotsUseCase getAvailableSlotsUseCase;
    private final CreateReservationUseCase createReservationUseCase;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final MutableLiveData<UiState<CreateReservationUiModel>> state = new MutableLiveData<>(UiState.idle());
    private final MutableLiveData<UiState<Reservation>> creationState = new MutableLiveData<>(UiState.idle());

    private YearMonth visibleMonth = YearMonth.now();
    private List<LocalDate> availableDays = Collections.emptyList();
    private LocalDate selectedDate;
    private List<ReservationSlot> availableSlots = Collections.emptyList();
    private ReservationSlot selectedSlot;

    public CreateReservationViewModel(
            GetAvailableDaysUseCase getAvailableDaysUseCase,
            GetAvailableSlotsUseCase getAvailableSlotsUseCase,
            CreateReservationUseCase createReservationUseCase
    ) {
        this.getAvailableDaysUseCase = getAvailableDaysUseCase;
        this.getAvailableSlotsUseCase = getAvailableSlotsUseCase;
        this.createReservationUseCase = createReservationUseCase;
    }

    public LiveData<UiState<CreateReservationUiModel>> getState() {
        return state;
    }

    public LiveData<UiState<Reservation>> getCreationState() {
        return creationState;
    }

    public void loadMonth(YearMonth month) {
        visibleMonth = month;
        selectedDate = null;
        selectedSlot = null;
        availableSlots = Collections.emptyList();
        state.setValue(UiState.loading());
        executorService.execute(() -> {
            Result<List<LocalDate>> result = getAvailableDaysUseCase.execute(month.getYear(), month.getMonthValue());
            if (result.isSuccess()) {
                availableDays = result.getData();
                state.postValue(UiState.success(currentModel()));
            } else {
                state.postValue(UiState.error(result.getError()));
            }
        });
    }

    public void selectDate(LocalDate date) {
        selectedDate = date;
        selectedSlot = null;
        state.setValue(UiState.loading());
        executorService.execute(() -> {
            Result<List<ReservationSlot>> result = getAvailableSlotsUseCase.execute(date);
            if (result.isSuccess()) {
                availableSlots = result.getData();
                if (availableSlots == null || availableSlots.isEmpty()) {
                    state.postValue(UiState.empty("Nenhum horario disponivel para esta data.", currentModel()));
                } else {
                    state.postValue(UiState.success(currentModel()));
                }
            } else {
                state.postValue(UiState.error(result.getError()));
            }
        });
    }

    public void selectSlot(ReservationSlot slot) {
        selectedSlot = slot;
        state.setValue(UiState.success(currentModel()));
    }

    public void confirmReservation() {
        if (selectedDate == null || selectedSlot == null) {
            creationState.setValue(UiState.error("Selecione data e horario para confirmar."));
            return;
        }

        creationState.setValue(UiState.loading());
        executorService.execute(() -> {
            Result<Reservation> result = createReservationUseCase.execute(selectedDate, selectedSlot);
            if (result.isSuccess()) {
                creationState.postValue(UiState.success(result.getData()));
            } else {
                creationState.postValue(UiState.error(result.getError()));
            }
        });
    }

    private CreateReservationUiModel currentModel() {
        return new CreateReservationUiModel(
                visibleMonth,
                availableDays,
                selectedDate,
                availableSlots,
                selectedSlot
        );
    }

    @Override
    protected void onCleared() {
        executorService.shutdownNow();
    }
}
