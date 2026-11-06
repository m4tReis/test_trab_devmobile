package br.com.santosschein.complexo.interfaces.controller;

import br.com.santosschein.complexo.application.usecase.auth.LoginUseCase;
import br.com.santosschein.complexo.application.usecase.auth.LogoutUseCase;
import br.com.santosschein.complexo.application.usecase.auth.RegisterUserUseCase;
import br.com.santosschein.complexo.shared.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class AuthControllerValidationTest {

    private final LoginUseCase loginUseCase = mock(LoginUseCase.class);
    private final RegisterUserUseCase registerUserUseCase = mock(RegisterUserUseCase.class);
    private final LogoutUseCase logoutUseCase = mock(LogoutUseCase.class);
    private final MockMvc mockMvc = standaloneSetup(new AuthController(loginUseCase, registerUserUseCase, logoutUseCase))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    @Test
    void rejectsRegistrationWithInvalidData() throws Exception {
        String body = """
                {
                  "firstName": "",
                  "lastName": "",
                  "birthDate": null,
                  "document": "",
                  "email": "email-invalido",
                  "password": ""
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.path").value("/api/v1/auth/register"));

        verifyNoInteractions(registerUserUseCase);
    }

    @Test
    void rejectsPasswordWithoutSymbol() throws Exception {
        performRegisterWithPassword("Senha1234")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString(PASSWORD_ERROR)));

        verifyNoInteractions(registerUserUseCase);
    }

    @Test
    void rejectsPasswordWithoutNumber() throws Exception {
        performRegisterWithPassword("Senha@abc")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString(PASSWORD_ERROR)));

        verifyNoInteractions(registerUserUseCase);
    }

    @Test
    void rejectsShortPassword() throws Exception {
        performRegisterWithPassword("S@1abc")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString(PASSWORD_ERROR)));

        verifyNoInteractions(registerUserUseCase);
    }

    @Test
    void acceptsStrongPassword() throws Exception {
        // birthDate nulo forca um 400, mas sem erro no campo password.
        performRegisterWithPassword("Senha@123")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(not(containsString(PASSWORD_ERROR))));
    }

    @Test
    void acceptsEmailWithSurroundingSpacesAndIgnoresRoleField() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "Joao",
                                  "lastName": "Silva",
                                  "birthDate": "1995-04-20",
                                  "document": " 123.456.789-00 ",
                                  "email": "  Joao@Email.com ",
                                  "password": "Senha@123",
                                  "role": "ADMIN"
                                }
                                """))
                .andExpect(status().isCreated());

        verify(registerUserUseCase).execute(argThat(request ->
                request.email().equals("Joao@Email.com") && request.document().equals("123.456.789-00")));
    }

    @Test
    void loginSendsDeviceHeaderAndReturnsTokenInAuthorizationHeader() throws Exception {
        when(loginUseCase.execute(any(), eq("Android A"))).thenReturn("jwt-token");

        mockMvc.perform(post("/api/v1/auth/login")
                        .header("X-Device-Name", "Android A")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "joao@email.com", "password": "Senha@123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(header().string("Authorization", "Bearer jwt-token"));
    }

    @Test
    void logoutEndsCurrentSessionAndReturnsNoContent() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", "Bearer token-a1"))
                .andExpect(status().isNoContent());

        verify(logoutUseCase).execute("token-a1");
    }

    @Test
    void logoutWithoutTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Sessao expirada. Entre novamente."));

        verifyNoInteractions(logoutUseCase);
    }

    private static final String PASSWORD_ERROR = "password: A senha deve ter no minimo 8 caracteres";

    private ResultActions performRegisterWithPassword(String password) throws Exception {
        String body = """
                {
                  "firstName": "Joao",
                  "lastName": "Silva",
                  "birthDate": null,
                  "document": "12345678900",
                  "email": "joao@email.com",
                  "password": "%s"
                }
                """.formatted(password);

        return mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }
}
