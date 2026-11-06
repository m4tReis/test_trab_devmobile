package com.santosschein.app.data.mock;

import com.santosschein.app.core.error.AppError;
import com.santosschein.app.core.error.ErrorType;
import com.santosschein.app.core.session.SessionManager;
import com.santosschein.app.core.util.Result;
import com.santosschein.app.domain.model.AuthSession;
import com.santosschein.app.domain.model.RegisterUserInput;
import com.santosschein.app.domain.model.User;
import com.santosschein.app.domain.repository.AuthRepository;

import java.util.Locale;

public final class MockAuthRepository implements AuthRepository {

    private final MockDataStore dataStore;
    private final SessionManager sessionManager;

    public MockAuthRepository(MockDataStore dataStore, SessionManager sessionManager) {
        this.dataStore = dataStore;
        this.sessionManager = sessionManager;
    }

    /**
     * Aceita a conta fixa de demonstracao e tambem as contas criadas no cadastro simulado.
     */
    @Override
    public Result<AuthSession> login(String email, String password) {
        String normalizedEmail = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        String expectedPassword = dataStore.getPasswordsByEmail().get(normalizedEmail);
        User user = findUser(normalizedEmail);

        if (expectedPassword == null || user == null || !expectedPassword.equals(password)) {
            return Result.failure(new AppError(ErrorType.UNAUTHORIZED, "E-mail ou senha invalidos.", "401", null));
        }

        sessionManager.saveJwtToken(MockDataStore.MOCK_TOKEN);
        return Result.success(new AuthSession(MockDataStore.MOCK_TOKEN, user));
    }

    @Override
    public Result<Void> register(RegisterUserInput input) {
        String email = input.getEmail().trim().toLowerCase(Locale.ROOT);
        String document = input.getDocument().replaceAll("\\D", "");
        for (User user : dataStore.getUsers()) {
            if (user.getEmail().equalsIgnoreCase(email)
                    || user.getDocument().equals(document)) {
                return Result.failure(new AppError(
                        ErrorType.CONFLICT,
                        "E-mail ou documento ja cadastrado.",
                        "409",
                        null
                ));
            }
        }

        User user = new User(
                "user_" + (dataStore.getUsers().size() + 1),
                input.getFirstName(),
                input.getLastName(),
                input.getBirthDate(),
                document,
                email
        );
        dataStore.getUsers().add(user);
        dataStore.getPasswordsByEmail().put(email, input.getPassword());
        return Result.success(null);
    }

    @Override
    public Result<Void> logout() {
        sessionManager.clearSession();
        return Result.success(null);
    }

    private User findUser(String normalizedEmail) {
        for (User user : dataStore.getUsers()) {
            if (user.getEmail().equalsIgnoreCase(normalizedEmail)) {
                return user;
            }
        }
        return null;
    }
}
