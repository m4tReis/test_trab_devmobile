package com.santosschein.app;

import com.santosschein.app.core.error.ErrorType;
import com.santosschein.app.core.network.HttpErrorMapper;
import com.santosschein.app.core.session.SessionManager;
import com.santosschein.app.core.session.SessionStorage;
import com.santosschein.app.core.util.Result;
import com.santosschein.app.data.mock.MockAuthRepository;
import com.santosschein.app.data.mock.MockDataStore;
import com.santosschein.app.data.mock.MockReservationRepository;
import com.santosschein.app.data.mock.MockUserRepository;
import com.santosschein.app.data.remote.api.AuthApi;
import com.santosschein.app.data.remote.dto.LoginRequestDto;
import com.santosschein.app.data.remote.dto.RegisterUserRequestDto;
import com.santosschein.app.data.repository.RemoteAuthRepository;
import com.santosschein.app.domain.model.AuthSession;
import com.santosschein.app.domain.model.RegisterUserInput;
import com.santosschein.app.domain.model.Reservation;
import com.santosschein.app.domain.model.ReservationSlot;
import com.santosschein.app.domain.model.ReservationStatus;
import com.santosschein.app.domain.model.User;

import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.ResponseBody;
import okio.Timeout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class DataLayerTest {

    @Test
    public void mockLoginWithValidCredentialsSavesToken() {
        FakeSessionStorage storage = new FakeSessionStorage();
        MockAuthRepository repository = new MockAuthRepository(new MockDataStore(), new SessionManager(storage));

        Result<AuthSession> result = repository.login(MockDataStore.VALID_EMAIL, MockDataStore.VALID_PASSWORD);

        assertTrue(result.isSuccess());
        assertEquals(MockDataStore.MOCK_TOKEN, storage.getJwtToken());
        assertNotNull(result.getData().getUser());
    }

    @Test
    public void mockLoginWithInvalidCredentialsReturnsUnauthorized() {
        MockAuthRepository repository = new MockAuthRepository(new MockDataStore(), new SessionManager(new FakeSessionStorage()));

        Result<AuthSession> result = repository.login(MockDataStore.VALID_EMAIL, "wrong");

        assertFalse(result.isSuccess());
        assertEquals(ErrorType.UNAUTHORIZED, result.getError().getType());
    }

    @Test
    public void mockRegisterWithNewUserSucceeds() {
        MockAuthRepository repository = new MockAuthRepository(new MockDataStore(), new SessionManager(new FakeSessionStorage()));
        RegisterUserInput input = new RegisterUserInput(
                "Maria",
                "Souza",
                LocalDate.of(1992, 5, 12),
                "98765432100",
                "maria@email.com",
                "Senha@123"
        );

        Result<Void> result = repository.register(input);

        assertTrue(result.isSuccess());
    }

    @Test
    public void mockRegisterWithExistingDocumentReturnsConflict() {
        MockAuthRepository repository = new MockAuthRepository(new MockDataStore(), new SessionManager(new FakeSessionStorage()));
        RegisterUserInput input = new RegisterUserInput(
                "Joao",
                "Silva",
                LocalDate.of(1995, 4, 20),
                "12345678900",
                "novo@email.com",
                "Senha@123"
        );

        Result<Void> result = repository.register(input);

        assertFalse(result.isSuccess());
        assertEquals(ErrorType.CONFLICT, result.getError().getType());
    }

    @Test
    public void mockRegisterWithExistingEmailReturnsConflict() {
        MockAuthRepository repository = new MockAuthRepository(new MockDataStore(), new SessionManager(new FakeSessionStorage()));
        RegisterUserInput input = new RegisterUserInput(
                "Ana",
                "Costa",
                LocalDate.of(1993, 8, 4),
                "22233344455",
                MockDataStore.VALID_EMAIL,
                "Senha@123"
        );

        Result<Void> result = repository.register(input);

        assertFalse(result.isSuccess());
        assertEquals(ErrorType.CONFLICT, result.getError().getType());
    }

    @Test
    public void mockLogoutClearsSession() {
        FakeSessionStorage storage = new FakeSessionStorage();
        SessionManager sessionManager = new SessionManager(storage);
        MockAuthRepository repository = new MockAuthRepository(new MockDataStore(), sessionManager);
        repository.login(MockDataStore.VALID_EMAIL, MockDataStore.VALID_PASSWORD);

        repository.logout();

        assertFalse(storage.hasActiveSession());
    }

    @Test
    public void mockNextReservationReturnsUpcomingReservation() {
        MockReservationRepository repository = new MockReservationRepository(new MockDataStore());

        Result<Reservation> result = repository.getNextReservation();

        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertEquals("res_123", result.getData().getId());
    }

    @Test
    public void mockReservationCreationConsumesSlotAndSecondTryConflicts() {
        MockDataStore dataStore = new MockDataStore();
        MockReservationRepository repository = new MockReservationRepository(dataStore);
        LocalDate date = firstAvailableDate(dataStore);

        Result<Reservation> firstResult = repository.createReservation(date, "slot_2");
        Result<Reservation> secondResult = repository.createReservation(date, "slot_2");

        assertTrue(firstResult.isSuccess());
        assertEquals(LocalTime.of(19, 0), firstResult.getData().getStartTime());
        assertEquals(ReservationStatus.ACTIVE, firstResult.getData().getStatus());
        assertFalse(secondResult.isSuccess());
        assertEquals(ErrorType.CONFLICT, secondResult.getError().getType());
    }

    @Test
    public void mockReservationCanSimulateNoNextReservation() {
        MockDataStore dataStore = new MockDataStore();
        dataStore.clearUpcomingReservations();
        MockReservationRepository repository = new MockReservationRepository(dataStore);

        Result<Reservation> result = repository.getNextReservation();

        assertTrue(result.isSuccess());
        assertNull(result.getData());
    }

    @Test
    public void mockReservationListsDaysSlotsPastAndUpcomingReservations() {
        MockDataStore dataStore = new MockDataStore();
        MockReservationRepository repository = new MockReservationRepository(dataStore);
        LocalDate date = firstAvailableDate(dataStore);

        Result<List<LocalDate>> days = repository.getAvailableDays(date.getYear(), date.getMonthValue());
        Result<List<ReservationSlot>> slots = repository.getAvailableSlots(date);
        Result<List<Reservation>> upcoming = repository.getUpcomingReservations();
        Result<List<Reservation>> past = repository.getPastReservations();

        assertTrue(days.isSuccess());
        assertEquals(3, days.getData().size());
        assertTrue(slots.isSuccess());
        assertEquals(3, slots.getData().size());
        assertTrue(upcoming.isSuccess());
        assertFalse(upcoming.getData().isEmpty());
        assertTrue(past.isSuccess());
        assertFalse(past.getData().isEmpty());
    }

    @Test
    public void mockReservationCancelUnknownReservationReturnsNotFound() {
        MockReservationRepository repository = new MockReservationRepository(new MockDataStore());

        Result<Void> result = repository.cancelReservation("missing");

        assertFalse(result.isSuccess());
        assertEquals(ErrorType.NOT_FOUND, result.getError().getType());
    }

    @Test
    public void mockReservationCancelExistingReservationMarksItAsCancelled() {
        MockDataStore dataStore = new MockDataStore();
        MockReservationRepository repository = new MockReservationRepository(dataStore);

        Result<Void> result = repository.cancelReservation("res_123");

        assertTrue(result.isSuccess());
        assertEquals(ReservationStatus.CANCELLED, dataStore.getUpcomingReservations().get(0).getStatus());
    }

    @Test
    public void mockReservationCancelInsideLeadTimeReturnsConflict() {
        MockDataStore dataStore = new MockDataStore();
        dataStore.getUpcomingReservations().add(new Reservation(
                "res_late",
                "Campo 1",
                LocalDate.now(),
                LocalTime.of(21, 0),
                LocalTime.of(22, 0),
                new BigDecimal("150.00"),
                ReservationStatus.ACTIVE
        ));
        MockReservationRepository repository = new MockReservationRepository(dataStore);

        Result<Void> result = repository.cancelReservation("res_late");

        assertFalse(result.isSuccess());
        assertEquals(ErrorType.CONFLICT, result.getError().getType());
    }

    @Test
    public void mockProfileReturnsDefaultUser() {
        MockUserRepository repository = new MockUserRepository(new MockDataStore());

        Result<User> result = repository.getProfile();

        assertTrue(result.isSuccess());
        assertEquals(MockDataStore.VALID_EMAIL, result.getData().getEmail());
    }

    @Test
    public void httpErrorMapperMapsExpectedStatusCodes() {
        HttpErrorMapper mapper = new HttpErrorMapper();

        assertEquals(ErrorType.VALIDATION, mapper.fromHttpCode(400).getType());
        assertEquals("400", mapper.fromHttpCode(400).getCode());
        assertEquals(ErrorType.UNAUTHORIZED, mapper.fromHttpCode(401).getType());
        assertEquals(ErrorType.FORBIDDEN, mapper.fromHttpCode(403).getType());
        assertEquals(ErrorType.NOT_FOUND, mapper.fromHttpCode(404).getType());
        assertEquals(ErrorType.CONFLICT, mapper.fromHttpCode(409).getType());
        assertEquals(ErrorType.UNKNOWN, mapper.fromHttpCode(500).getType());
    }

    @Test
    public void remoteLoginStoresAuthorizationHeaderToken() {
        FakeSessionStorage storage = new FakeSessionStorage();
        AuthApi authApi = new FakeAuthApi(Response.success(null, Headers.of("Authorization", "Bearer remote-token")));
        RemoteAuthRepository repository = new RemoteAuthRepository(
                authApi,
                new SessionManager(storage),
                new HttpErrorMapper()
        );

        Result<AuthSession> result = repository.login("usuario@email.com", "senha");

        assertTrue(result.isSuccess());
        assertEquals("remote-token", storage.getJwtToken());
        assertEquals("remote-token", result.getData().getJwtToken());
    }

    @Test
    public void remoteLoginWithWrongPasswordShowsCredentialsMessage() {
        AuthApi authApi = new FakeAuthApi(Response.error(401, ResponseBody.create("", (MediaType) null)));
        RemoteAuthRepository repository = new RemoteAuthRepository(
                authApi,
                new SessionManager(new FakeSessionStorage()),
                new HttpErrorMapper()
        );

        Result<AuthSession> result = repository.login("usuario@email.com", "errada");

        assertFalse(result.isSuccess());
        assertEquals(ErrorType.UNAUTHORIZED, result.getError().getType());
        assertEquals(HttpErrorMapper.INVALID_CREDENTIALS_MESSAGE, result.getError().getMessage());
    }

    @Test
    public void unauthorizedOutsideLoginMeansExpiredSession() {
        assertEquals(HttpErrorMapper.SESSION_EXPIRED_MESSAGE, new HttpErrorMapper().fromHttpCode(401).getMessage());
    }

    @Test
    public void remoteLogoutEndsSessionOnServerAndClearsLocalToken() {
        FakeSessionStorage storage = new FakeSessionStorage();
        storage.saveJwtToken("token-a1");
        FakeAuthApi authApi = new FakeAuthApi(Response.success(null));
        RemoteAuthRepository repository = new RemoteAuthRepository(authApi, new SessionManager(storage), new HttpErrorMapper());

        Result<Void> result = repository.logout();

        assertTrue(authApi.logoutCalled);
        assertTrue(result.isSuccess());
        assertFalse(storage.hasActiveSession());
    }

    @Test
    public void remoteLogoutWithoutConnectionLeavesDeviceButWarnsUser() {
        FakeSessionStorage storage = new FakeSessionStorage();
        storage.saveJwtToken("token-a1");
        FakeAuthApi authApi = new FakeAuthApi(Response.success(null), new OfflineCall<>());
        RemoteAuthRepository repository = new RemoteAuthRepository(authApi, new SessionManager(storage), new HttpErrorMapper());

        Result<Void> result = repository.logout();

        assertFalse(result.isSuccess());
        assertEquals(ErrorType.NETWORK, result.getError().getType());
        assertEquals(RemoteAuthRepository.LOGOUT_NOT_CONFIRMED_MESSAGE, result.getError().getMessage());
        assertFalse(storage.hasActiveSession());
    }

    @Test
    public void remoteLogoutWithSessionAlreadyClosedOnServerIsSuccess() {
        FakeSessionStorage storage = new FakeSessionStorage();
        storage.saveJwtToken("token-a1");
        FakeAuthApi authApi = new FakeAuthApi(
                Response.success(null),
                new ImmediateCall<>(Response.error(401, ResponseBody.create("", (MediaType) null)))
        );
        RemoteAuthRepository repository = new RemoteAuthRepository(authApi, new SessionManager(storage), new HttpErrorMapper());

        assertTrue(repository.logout().isSuccess());
        assertFalse(storage.hasActiveSession());
    }

    @Test
    public void sessionExpiredNoticeIsShownOnlyOnce() {
        FakeSessionStorage storage = new FakeSessionStorage();
        SessionManager sessionManager = new SessionManager(storage);
        sessionManager.saveJwtToken("token-vencido");

        sessionManager.markSessionExpired();

        assertFalse(storage.hasActiveSession());
        assertTrue(sessionManager.consumeSessionExpiredNotice());
        assertFalse(sessionManager.consumeSessionExpiredNotice());
    }

    @Test
    public void sessionExpiredNoticeIsNotRaisedWithoutSavedSession() {
        SessionManager sessionManager = new SessionManager(new FakeSessionStorage());

        sessionManager.markSessionExpired();

        assertFalse(sessionManager.consumeSessionExpiredNotice());
    }

    @Test
    public void mockAccountCreatedInRegisterCanLogIn() {
        FakeSessionStorage storage = new FakeSessionStorage();
        MockAuthRepository repository = new MockAuthRepository(new MockDataStore(), new SessionManager(storage));
        repository.register(new RegisterUserInput(
                "Maria",
                "Souza",
                LocalDate.of(1992, 5, 12),
                "987.654.321-00",
                "Maria@Email.com",
                "Senha@123"
        ));

        Result<AuthSession> wrongPassword = repository.login("maria@email.com", "Outra@123");
        Result<AuthSession> result = repository.login("MARIA@email.com", "Senha@123");

        assertFalse(wrongPassword.isSuccess());
        assertTrue(result.isSuccess());
        assertEquals("Maria", result.getData().getUser().getFirstName());
        assertEquals("98765432100", result.getData().getUser().getDocument());
        assertTrue(storage.hasActiveSession());
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

    private static LocalDate firstAvailableDate(MockDataStore dataStore) {
        return dataStore.getSlotsByDate().keySet().stream()
                .sorted()
                .findFirst()
                .orElseThrow(AssertionError::new);
    }

    private static final class FakeAuthApi implements AuthApi {

        private final Response<Void> loginResponse;
        private final Call<Void> logoutCall;
        boolean logoutCalled;

        FakeAuthApi(Response<Void> loginResponse) {
            this(loginResponse, new ImmediateCall<>(Response.success(null)));
        }

        FakeAuthApi(Response<Void> loginResponse, Call<Void> logoutCall) {
            this.loginResponse = loginResponse;
            this.logoutCall = logoutCall;
        }

        @Override
        public Call<Void> login(LoginRequestDto request) {
            return new ImmediateCall<>(loginResponse);
        }

        @Override
        public Call<Void> register(RegisterUserRequestDto request) {
            return new ImmediateCall<>(Response.success(null));
        }

        @Override
        public Call<Void> logout() {
            logoutCalled = true;
            return logoutCall;
        }
    }

    /** Simula a falta de conexao: execute() lanca IOException. */
    private static final class OfflineCall<T> implements Call<T> {

        @Override
        public Response<T> execute() throws IOException {
            throw new IOException("sem conexao");
        }

        @Override
        public void enqueue(Callback<T> callback) {
            callback.onFailure(this, new IOException("sem conexao"));
        }

        @Override
        public boolean isExecuted() {
            return false;
        }

        @Override
        public void cancel() {
        }

        @Override
        public boolean isCanceled() {
            return false;
        }

        @Override
        public Call<T> clone() {
            return new OfflineCall<>();
        }

        @Override
        public Request request() {
            return new Request.Builder().url("http://localhost/").build();
        }

        @Override
        public Timeout timeout() {
            return Timeout.NONE;
        }
    }

    private static final class ImmediateCall<T> implements Call<T> {

        private final Response<T> response;

        ImmediateCall(Response<T> response) {
            this.response = response;
        }

        @Override
        public Response<T> execute() throws IOException {
            return response;
        }

        @Override
        public void enqueue(Callback<T> callback) {
            callback.onResponse(this, response);
        }

        @Override
        public boolean isExecuted() {
            return false;
        }

        @Override
        public void cancel() {
        }

        @Override
        public boolean isCanceled() {
            return false;
        }

        @Override
        public Call<T> clone() {
            return new ImmediateCall<>(response);
        }

        @Override
        public Request request() {
            return new Request.Builder().url("http://localhost/").build();
        }

        @Override
        public Timeout timeout() {
            return Timeout.NONE;
        }
    }
}
