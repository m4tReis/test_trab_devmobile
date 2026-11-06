package com.santosschein.app;

import com.santosschein.app.core.error.ErrorType;
import com.santosschein.app.core.util.Result;
import com.santosschein.app.domain.model.AuthSession;
import com.santosschein.app.domain.model.RegisterUserInput;
import com.santosschein.app.domain.model.Reservation;
import com.santosschein.app.domain.model.ReservationSlot;
import com.santosschein.app.domain.model.ReservationStatus;
import com.santosschein.app.domain.model.User;
import com.santosschein.app.domain.repository.AuthRepository;
import com.santosschein.app.domain.repository.ReservationRepository;
import com.santosschein.app.domain.repository.UserRepository;
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

import org.junit.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class DomainUseCaseTest {

    @Test
    public void loginWithValidDataDelegatesToRepository() {
        FakeAuthRepository repository = new FakeAuthRepository();
        LoginUseCase useCase = new LoginUseCase(repository);

        Result<AuthSession> result = useCase.execute(" user@email.com ", "123456");

        assertTrue(result.isSuccess());
        assertTrue(repository.loginCalled);
        assertEquals("user@email.com", repository.lastEmail);
        assertEquals("123456", repository.lastPassword);
        assertEquals("jwt-token", result.getData().getJwtToken());
    }

    @Test
    public void loginWithInvalidEmailReturnsValidationError() {
        FakeAuthRepository repository = new FakeAuthRepository();
        LoginUseCase useCase = new LoginUseCase(repository);

        Result<AuthSession> result = useCase.execute("invalid-email", "123456");

        assertFalse(result.isSuccess());
        assertFalse(repository.loginCalled);
        assertNotNull(result.getError());
        assertEquals(ErrorType.VALIDATION, result.getError().getType());
    }

    @Test
    public void registerWithFutureBirthDateReturnsValidationError() {
        FakeAuthRepository repository = new FakeAuthRepository();
        RegisterUserUseCase useCase = new RegisterUserUseCase(repository);
        RegisterUserInput input = new RegisterUserInput(
                "Ana",
                "Santos",
                LocalDate.now().plusDays(1),
                "12345678900",
                "ana@email.com",
                "Senha@123"
        );

        Result<Void> result = useCase.execute(input);

        assertFalse(result.isSuccess());
        assertFalse(repository.registerCalled);
        assertNotNull(result.getError());
        assertEquals(ErrorType.VALIDATION, result.getError().getType());
    }

    @Test
    public void registerWithValidDataDelegatesToRepository() {
        FakeAuthRepository repository = new FakeAuthRepository();
        RegisterUserUseCase useCase = new RegisterUserUseCase(repository);
        RegisterUserInput input = new RegisterUserInput(
                "Ana",
                "Santos",
                LocalDate.of(1998, 3, 10),
                "12345678900",
                "ana@email.com",
                "Senha@123"
        );

        Result<Void> result = useCase.execute(input);

        assertTrue(result.isSuccess());
        assertTrue(repository.registerCalled);
        assertSame(input, repository.lastRegisterInput);
    }

    @Test
    public void registerWithShortPasswordReturnsValidationError() {
        assertRegisterValidationError(validRegisterInputWithPassword("Se1@"));
    }

    @Test
    public void registerWithPasswordWithoutLetterReturnsValidationError() {
        assertRegisterValidationError(validRegisterInputWithPassword("12345678@"));
    }

    @Test
    public void registerWithPasswordWithoutNumberReturnsValidationError() {
        assertRegisterValidationError(validRegisterInputWithPassword("Senha@@@"));
    }

    @Test
    public void registerWithPasswordWithoutSymbolReturnsValidationError() {
        assertRegisterValidationError(validRegisterInputWithPassword("Senha123"));
    }

    @Test
    public void registerWithIncompleteCpfReturnsValidationError() {
        FakeAuthRepository repository = new FakeAuthRepository();
        RegisterUserUseCase useCase = new RegisterUserUseCase(repository);
        RegisterUserInput input = new RegisterUserInput(
                "Ana",
                "Santos",
                LocalDate.of(1998, 3, 10),
                "1234567890",
                "ana@email.com",
                "Senha@123"
        );

        Result<Void> result = useCase.execute(input);

        assertFalse(result.isSuccess());
        assertFalse(repository.registerCalled);
        assertEquals(ErrorType.VALIDATION, result.getError().getType());
    }

    @Test
    public void logoutDelegatesToRepository() {
        FakeAuthRepository repository = new FakeAuthRepository();
        LogoutUseCase useCase = new LogoutUseCase(repository);

        Result<Void> result = useCase.execute();

        assertTrue(repository.logoutCalled);
        assertTrue(result.isSuccess());
    }

    @Test
    public void createReservationWithBlankSlotReturnsValidationError() {
        FakeReservationRepository repository = new FakeReservationRepository();
        CreateReservationUseCase useCase = new CreateReservationUseCase(repository);

        Result<Reservation> result = useCase.execute(LocalDate.now().plusDays(7), " ");

        assertFalse(result.isSuccess());
        assertFalse(repository.createReservationCalled);
        assertNotNull(result.getError());
        assertEquals(ErrorType.VALIDATION, result.getError().getType());
    }

    @Test
    public void createReservationWithPastDateReturnsValidationError() {
        FakeReservationRepository repository = new FakeReservationRepository();
        CreateReservationUseCase useCase = new CreateReservationUseCase(repository);

        Result<Reservation> result = useCase.execute(LocalDate.now().minusDays(1), "slot-19");

        assertFalse(result.isSuccess());
        assertFalse(repository.createReservationCalled);
        assertNotNull(result.getError());
        assertEquals(ErrorType.VALIDATION, result.getError().getType());
    }

    @Test
    public void createReservationTodayWithPastSlotReturnsValidationError() {
        FakeReservationRepository repository = new FakeReservationRepository();
        CreateReservationUseCase useCase = new CreateReservationUseCase(repository, fixedClock());
        ReservationSlot pastSlot = createSlot("slot-14", LocalTime.of(14, 0));

        Result<Reservation> result = useCase.execute(LocalDate.of(2026, 7, 3), pastSlot);

        assertFalse(result.isSuccess());
        assertFalse(repository.createReservationCalled);
        assertNotNull(result.getError());
        assertEquals(ErrorType.VALIDATION, result.getError().getType());
    }

    @Test
    public void createReservationWithValidDataTrimsSlotAndDelegates() {
        FakeReservationRepository repository = new FakeReservationRepository();
        CreateReservationUseCase useCase = new CreateReservationUseCase(repository);
        LocalDate date = LocalDate.now().plusDays(7);

        Result<Reservation> result = useCase.execute(date, " slot-19 ");

        assertTrue(result.isSuccess());
        assertTrue(repository.createReservationCalled);
        assertEquals(date, repository.lastDate);
        assertEquals("slot-19", repository.lastSlotId);
    }

    @Test
    public void readReservationUseCasesDelegateToRepository() {
        FakeReservationRepository repository = new FakeReservationRepository();

        assertTrue(new GetNextReservationUseCase(repository).execute().isSuccess());
        assertTrue(new GetAvailableDaysUseCase(repository).execute(2026, 7).isSuccess());
        assertTrue(new GetAvailableSlotsUseCase(repository).execute(LocalDate.now().plusDays(7)).isSuccess());
        assertTrue(new GetUpcomingReservationsUseCase(repository).execute().isSuccess());
        assertTrue(new GetPastReservationsUseCase(repository).execute().isSuccess());

        assertTrue(repository.nextReservationCalled);
        assertTrue(repository.availableDaysCalled);
        assertTrue(repository.availableSlotsCalled);
        assertTrue(repository.upcomingReservationsCalled);
        assertTrue(repository.pastReservationsCalled);
    }

    @Test
    public void getAvailableSlotsWithPastDateReturnsValidationError() {
        FakeReservationRepository repository = new FakeReservationRepository();
        GetAvailableSlotsUseCase useCase = new GetAvailableSlotsUseCase(repository);

        Result<List<ReservationSlot>> result = useCase.execute(LocalDate.now().minusDays(1));

        assertFalse(result.isSuccess());
        assertFalse(repository.availableSlotsCalled);
        assertNotNull(result.getError());
        assertEquals(ErrorType.VALIDATION, result.getError().getType());
    }

    @Test
    public void getAvailableSlotsForTodayFiltersPastSlots() {
        FakeReservationRepository repository = new FakeReservationRepository();
        repository.availableSlots = Arrays.asList(
                createSlot("slot-14", LocalTime.of(14, 0)),
                createSlot("slot-16", LocalTime.of(16, 0))
        );
        GetAvailableSlotsUseCase useCase = new GetAvailableSlotsUseCase(repository, fixedClock());

        Result<List<ReservationSlot>> result = useCase.execute(LocalDate.of(2026, 7, 3));

        assertTrue(result.isSuccess());
        assertTrue(repository.availableSlotsCalled);
        assertEquals(1, result.getData().size());
        assertEquals("slot-16", result.getData().get(0).getId());
    }

    @Test
    public void cancelReservationTrimsIdAndDelegates() {
        FakeReservationRepository repository = new FakeReservationRepository();
        CancelReservationUseCase useCase = new CancelReservationUseCase(repository);

        Result<Void> result = useCase.execute(" reservation-1 ");

        assertTrue(result.isSuccess());
        assertTrue(repository.cancelReservationCalled);
        assertEquals("reservation-1", repository.lastReservationId);
    }

    @Test
    public void getProfileDelegatesToRepository() {
        FakeUserRepository repository = new FakeUserRepository();
        GetProfileUseCase useCase = new GetProfileUseCase(repository);

        Result<User> result = useCase.execute();

        assertTrue(result.isSuccess());
        assertTrue(repository.profileCalled);
        assertEquals("Ana", result.getData().getFirstName());
    }

    private static final class FakeAuthRepository implements AuthRepository {

        boolean loginCalled;
        boolean registerCalled;
        boolean logoutCalled;
        String lastEmail;
        String lastPassword;
        RegisterUserInput lastRegisterInput;

        @Override
        public Result<AuthSession> login(String email, String password) {
            loginCalled = true;
            lastEmail = email;
            lastPassword = password;
            return Result.success(new AuthSession("jwt-token", createUser()));
        }

        @Override
        public Result<Void> register(RegisterUserInput input) {
            registerCalled = true;
            lastRegisterInput = input;
            return Result.success(null);
        }

        @Override
        public Result<Void> logout() {
            logoutCalled = true;
            return Result.success(null);
        }
    }

    private static final class FakeReservationRepository implements ReservationRepository {

        boolean nextReservationCalled;
        boolean availableDaysCalled;
        boolean availableSlotsCalled;
        boolean createReservationCalled;
        boolean upcomingReservationsCalled;
        boolean pastReservationsCalled;
        boolean cancelReservationCalled;
        LocalDate lastDate;
        String lastSlotId;
        String lastReservationId;
        List<ReservationSlot> availableSlots;

        @Override
        public Result<Reservation> getNextReservation() {
            nextReservationCalled = true;
            return Result.success(DomainUseCaseTest.createReservation());
        }

        @Override
        public Result<List<LocalDate>> getAvailableDays(int year, int month) {
            availableDaysCalled = true;
            return Result.success(Collections.singletonList(LocalDate.of(year, month, 10)));
        }

        @Override
        public Result<List<ReservationSlot>> getAvailableSlots(LocalDate date) {
            availableSlotsCalled = true;
            lastDate = date;
            if (availableSlots != null) {
                return Result.success(availableSlots);
            }
            return Result.success(Collections.singletonList(createSlot("slot-19", LocalTime.of(19, 0))));
        }

        @Override
        public Result<Reservation> createReservation(LocalDate date, String slotId) {
            createReservationCalled = true;
            lastDate = date;
            lastSlotId = slotId;
            return Result.success(DomainUseCaseTest.createReservation());
        }

        @Override
        public Result<List<Reservation>> getUpcomingReservations() {
            upcomingReservationsCalled = true;
            return Result.success(Collections.singletonList(DomainUseCaseTest.createReservation()));
        }

        @Override
        public Result<List<Reservation>> getPastReservations() {
            pastReservationsCalled = true;
            return Result.success(Collections.emptyList());
        }

        @Override
        public Result<Void> cancelReservation(String reservationId) {
            cancelReservationCalled = true;
            lastReservationId = reservationId;
            return Result.success(null);
        }
    }

    private static final class FakeUserRepository implements UserRepository {

        boolean profileCalled;

        @Override
        public Result<User> getProfile() {
            profileCalled = true;
            return Result.success(createUser());
        }
    }

    private static User createUser() {
        return new User(
                "user-1",
                "Ana",
                "Santos",
                LocalDate.of(1998, 3, 10),
                "12345678900",
                "ana@email.com"
        );
    }

    private static Reservation createReservation() {
        return new Reservation(
                "reservation-1",
                "Campo Society 1",
                LocalDate.of(2026, 7, 10),
                LocalTime.of(19, 0),
                LocalTime.of(20, 0),
                new BigDecimal("120.00"),
                ReservationStatus.ACTIVE
        );
    }

    private static ReservationSlot createSlot(String id, LocalTime startTime) {
        return new ReservationSlot(
                id,
                startTime,
                startTime.plusHours(1),
                new BigDecimal("120.00")
        );
    }

    private static Clock fixedClock() {
        return Clock.fixed(Instant.parse("2026-07-03T18:00:00Z"), ZoneId.of("America/Sao_Paulo"));
    }

    private static RegisterUserInput validRegisterInputWithPassword(String password) {
        return new RegisterUserInput(
                "Ana",
                "Santos",
                LocalDate.of(1998, 3, 10),
                "12345678900",
                "ana@email.com",
                password
        );
    }

    private static void assertRegisterValidationError(RegisterUserInput input) {
        FakeAuthRepository repository = new FakeAuthRepository();
        RegisterUserUseCase useCase = new RegisterUserUseCase(repository);

        Result<Void> result = useCase.execute(input);

        assertFalse(result.isSuccess());
        assertFalse(repository.registerCalled);
        assertEquals(ErrorType.VALIDATION, result.getError().getType());
    }
}
