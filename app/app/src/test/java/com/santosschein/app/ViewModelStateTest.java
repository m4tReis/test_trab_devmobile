package com.santosschein.app;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.santosschein.app.core.error.AppError;
import com.santosschein.app.core.error.ErrorType;
import com.santosschein.app.core.session.SessionManager;
import com.santosschein.app.core.session.SessionStorage;
import com.santosschein.app.core.util.Result;
import com.santosschein.app.data.mock.MockAuthRepository;
import com.santosschein.app.data.mock.MockDataStore;
import com.santosschein.app.domain.model.Reservation;
import com.santosschein.app.domain.model.ReservationSlot;
import com.santosschein.app.domain.repository.ReservationRepository;
import com.santosschein.app.domain.usecase.CancelReservationUseCase;
import com.santosschein.app.domain.usecase.CreateReservationUseCase;
import com.santosschein.app.domain.usecase.GetAvailableDaysUseCase;
import com.santosschein.app.domain.usecase.GetAvailableSlotsUseCase;
import com.santosschein.app.domain.usecase.GetPastReservationsUseCase;
import com.santosschein.app.domain.usecase.GetUpcomingReservationsUseCase;
import com.santosschein.app.domain.usecase.LoginUseCase;
import com.santosschein.app.presentation.auth.login.LoginViewModel;
import com.santosschein.app.presentation.common.UiState;
import com.santosschein.app.presentation.reservation.create.CreateReservationViewModel;
import com.santosschein.app.presentation.reservation.list.ReservationListViewModel;

import org.junit.Rule;
import org.junit.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ViewModelStateTest {

    @Rule
    public final InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Test
    public void loginViewModelEmitsLoadingAndSuccess() throws InterruptedException {
        LoginViewModel viewModel = new LoginViewModel(new LoginUseCase(
                new MockAuthRepository(new MockDataStore(), new SessionManager(new FakeSessionStorage()))
        ));

        List<UiState.Status> statuses = collectStatuses(
                viewModel.getLoginState(),
                2,
                () -> viewModel.login(MockDataStore.VALID_EMAIL, MockDataStore.VALID_PASSWORD)
        );

        assertEquals(Arrays.asList(UiState.Status.LOADING, UiState.Status.SUCCESS), statuses);
    }

    @Test
    public void loginViewModelEmitsLoadingAndError() throws InterruptedException {
        LoginViewModel viewModel = new LoginViewModel(new LoginUseCase(
                new MockAuthRepository(new MockDataStore(), new SessionManager(new FakeSessionStorage()))
        ));

        List<UiState.Status> statuses = collectStatuses(
                viewModel.getLoginState(),
                2,
                () -> viewModel.login(MockDataStore.VALID_EMAIL, "senha-errada")
        );

        assertEquals(Arrays.asList(UiState.Status.LOADING, UiState.Status.ERROR), statuses);
    }

    @Test
    public void reservationListViewModelEmitsLoadingAndEmpty() throws InterruptedException {
        EmptyReservationRepository repository = new EmptyReservationRepository();
        ReservationListViewModel viewModel = new ReservationListViewModel(
                new GetUpcomingReservationsUseCase(repository),
                new GetPastReservationsUseCase(repository),
                new CancelReservationUseCase(repository)
        );

        List<UiState.Status> statuses = collectStatuses(
                viewModel.getReservationsState(),
                2,
                () -> viewModel.load(false)
        );

        assertEquals(Arrays.asList(UiState.Status.LOADING, UiState.Status.EMPTY), statuses);
    }

    @Test
    public void createReservationViewModelEmitsLoadingAndEmptyForDayWithoutSlots() throws InterruptedException {
        EmptyReservationRepository repository = new EmptyReservationRepository();
        CreateReservationViewModel viewModel = new CreateReservationViewModel(
                new GetAvailableDaysUseCase(repository),
                new GetAvailableSlotsUseCase(repository),
                new CreateReservationUseCase(repository)
        );

        List<UiState.Status> statuses = collectStatuses(
                viewModel.getState(),
                2,
                () -> viewModel.selectDate(LocalDate.now().plusDays(3))
        );

        assertEquals(Arrays.asList(UiState.Status.LOADING, UiState.Status.EMPTY), statuses);
    }

    private static <T> List<UiState.Status> collectStatuses(
            LiveData<UiState<T>> liveData,
            int expectedStatusCount,
            Runnable action
    ) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(expectedStatusCount);
        List<UiState.Status> statuses = new ArrayList<>();
        Observer<UiState<T>> observer = state -> {
            if (state != null && state.getStatus() != UiState.Status.IDLE) {
                statuses.add(state.getStatus());
                latch.countDown();
            }
        };

        liveData.observeForever(observer);
        try {
            action.run();
            assertTrue(latch.await(3, TimeUnit.SECONDS));
            return statuses;
        } finally {
            liveData.removeObserver(observer);
        }
    }

    private static final class FakeSessionStorage implements SessionStorage {

        private String token;

        @Override
        public void saveJwtToken(String token) {
            this.token = token;
        }

        @Override
        public String getJwtToken() {
            return token;
        }

        @Override
        public void clear() {
            token = null;
        }

        @Override
        public boolean hasActiveSession() {
            return token != null && !token.trim().isEmpty();
        }
    }

    private static final class EmptyReservationRepository implements ReservationRepository {

        @Override
        public Result<Reservation> getNextReservation() {
            return Result.success(null);
        }

        @Override
        public Result<List<LocalDate>> getAvailableDays(int year, int month) {
            return Result.success(Collections.emptyList());
        }

        @Override
        public Result<List<ReservationSlot>> getAvailableSlots(LocalDate date) {
            return Result.success(Collections.emptyList());
        }

        @Override
        public Result<Reservation> createReservation(LocalDate date, String slotId) {
            return Result.failure(new AppError(ErrorType.CONFLICT, "Esse horario nao esta mais disponivel."));
        }

        @Override
        public Result<List<Reservation>> getUpcomingReservations() {
            return Result.success(Collections.emptyList());
        }

        @Override
        public Result<List<Reservation>> getPastReservations() {
            return Result.success(Collections.emptyList());
        }

        @Override
        public Result<Void> cancelReservation(String reservationId) {
            return Result.failure(new AppError(ErrorType.CONFLICT, "Nao foi possivel cancelar."));
        }
    }
}
