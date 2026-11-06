package com.santosschein.app.core.config;

import android.content.Context;

import com.santosschein.app.core.network.HttpErrorMapper;
import com.santosschein.app.core.network.RetrofitClientFactory;
import com.santosschein.app.core.session.SessionManager;
import com.santosschein.app.core.session.SessionStorage;
import com.santosschein.app.core.session.SharedPreferencesSessionStorage;
import com.santosschein.app.data.mock.MockAuthRepository;
import com.santosschein.app.data.mock.MockDataStore;
import com.santosschein.app.data.mock.MockReservationRepository;
import com.santosschein.app.data.mock.MockUserRepository;
import com.santosschein.app.data.remote.api.ApiServiceFactory;
import com.santosschein.app.data.repository.RemoteAuthRepository;
import com.santosschein.app.data.repository.RemoteReservationRepository;
import com.santosschein.app.data.repository.RemoteUserRepository;
import com.santosschein.app.domain.repository.AuthRepository;
import com.santosschein.app.domain.repository.ReservationRepository;
import com.santosschein.app.domain.repository.UserRepository;
import com.santosschein.app.domain.usecase.GetInitialAppDestinationUseCase;
import lombok.Getter;

import retrofit2.Retrofit;

@Getter
public final class AppContainer {

    private final AppConfig appConfig;
    private final SessionStorage sessionStorage;
    private final SessionManager sessionManager;
    private final AuthRepository authRepository;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final GetInitialAppDestinationUseCase initialAppDestinationUseCase;

    public AppContainer(Context context) {
        appConfig = new AppConfig();
        sessionStorage = new SharedPreferencesSessionStorage(context);
        sessionManager = new SessionManager(sessionStorage);

        if (appConfig.isMockMode()) {
            MockDataStore mockDataStore = new MockDataStore();
            authRepository = new MockAuthRepository(mockDataStore, sessionManager);
            reservationRepository = new MockReservationRepository(mockDataStore);
            userRepository = new MockUserRepository(mockDataStore);
        } else {
            HttpErrorMapper errorMapper = new HttpErrorMapper();
            Retrofit retrofit = RetrofitClientFactory.create(appConfig, sessionManager);
            ApiServiceFactory apiServiceFactory = new ApiServiceFactory(retrofit);
            authRepository = new RemoteAuthRepository(
                    apiServiceFactory.createAuthApi(),
                    sessionManager,
                    errorMapper
            );
            reservationRepository = new RemoteReservationRepository(
                    apiServiceFactory.createReservationApi(),
                    sessionManager,
                    errorMapper
            );
            userRepository = new RemoteUserRepository(
                    apiServiceFactory.createUserApi(),
                    sessionManager,
                    errorMapper
            );
        }

        initialAppDestinationUseCase = new GetInitialAppDestinationUseCase(sessionStorage);
    }
}
