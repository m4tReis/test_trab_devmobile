package com.santosschein.app.presentation.common;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.santosschein.app.core.config.AppContainer;
import com.santosschein.app.domain.usecase.CancelReservationUseCase;
import com.santosschein.app.domain.usecase.CreateReservationUseCase;
import com.santosschein.app.domain.usecase.GetAvailableDaysUseCase;
import com.santosschein.app.domain.usecase.GetAvailableSlotsUseCase;
import com.santosschein.app.domain.usecase.GetNextReservationUseCase;
import com.santosschein.app.domain.usecase.GetPastReservationsUseCase;
import com.santosschein.app.domain.usecase.GetProfileUseCase;
import com.santosschein.app.domain.usecase.GetUpcomingReservationsUseCase;
import com.santosschein.app.domain.usecase.LoginUseCase;
import com.santosschein.app.domain.usecase.LogoutUseCase;
import com.santosschein.app.domain.usecase.RegisterUserUseCase;
import com.santosschein.app.presentation.auth.login.LoginViewModel;
import com.santosschein.app.presentation.auth.register.RegisterViewModel;
import com.santosschein.app.presentation.home.HomeViewModel;
import com.santosschein.app.presentation.profile.ProfileViewModel;
import com.santosschein.app.presentation.reservation.create.CreateReservationViewModel;
import com.santosschein.app.presentation.reservation.list.ReservationListViewModel;

public final class AppViewModelFactory implements ViewModelProvider.Factory {

    private final AppContainer appContainer;

    public AppViewModelFactory(AppContainer appContainer) {
        this.appContainer = appContainer;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(LoginViewModel.class)) {
            return (T) new LoginViewModel(new LoginUseCase(appContainer.getAuthRepository()));
        }
        if (modelClass.isAssignableFrom(RegisterViewModel.class)) {
            return (T) new RegisterViewModel(new RegisterUserUseCase(appContainer.getAuthRepository()));
        }
        if (modelClass.isAssignableFrom(HomeViewModel.class)) {
            return (T) new HomeViewModel(
                    new GetProfileUseCase(appContainer.getUserRepository()),
                    new GetNextReservationUseCase(appContainer.getReservationRepository())
            );
        }
        if (modelClass.isAssignableFrom(CreateReservationViewModel.class)) {
            return (T) new CreateReservationViewModel(
                    new GetAvailableDaysUseCase(appContainer.getReservationRepository()),
                    new GetAvailableSlotsUseCase(appContainer.getReservationRepository()),
                    new CreateReservationUseCase(appContainer.getReservationRepository())
            );
        }
        if (modelClass.isAssignableFrom(ReservationListViewModel.class)) {
            return (T) new ReservationListViewModel(
                    new GetUpcomingReservationsUseCase(appContainer.getReservationRepository()),
                    new GetPastReservationsUseCase(appContainer.getReservationRepository()),
                    new CancelReservationUseCase(appContainer.getReservationRepository())
            );
        }
        if (modelClass.isAssignableFrom(ProfileViewModel.class)) {
            return (T) new ProfileViewModel(
                    new GetProfileUseCase(appContainer.getUserRepository()),
                    new LogoutUseCase(appContainer.getAuthRepository())
            );
        }
        throw new IllegalArgumentException("ViewModel nao suportado: " + modelClass.getName());
    }
}
