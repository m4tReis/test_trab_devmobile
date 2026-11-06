package br.com.santosschein.complexo.shared.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void mapsInvalidCredentialsToUnauthorized() {
        var response = handler.handleInvalidCredentials(new InvalidCredentialsException(), request("/api/v1/auth/login"));

        assertThat(response.getStatusCode().value()).isEqualTo(401);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(401);
        assertThat(response.getBody().error()).isEqualTo("Unauthorized");
        assertThat(response.getBody().message()).isEqualTo("E-mail ou senha invalidos.");
        assertThat(response.getBody().path()).isEqualTo("/api/v1/auth/login");
    }

    @Test
    void mapsForbiddenOperationToForbidden() {
        var response = handler.handleForbiddenOperation(
                new ForbiddenOperationException("Reserva pertence a outro usuario."),
                request("/api/v1/users/me/reservations/res_1")
        );

        assertThat(response.getStatusCode().value()).isEqualTo(403);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Sem permissao.");
    }

    @Test
    void mapsNotFoundToNotFound() {
        var response = handler.handleResourceNotFound(
                new ResourceNotFoundException("Reserva nao encontrada."),
                request("/api/v1/users/me/reservations/res_1")
        );

        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Reserva nao encontrada.");
    }

    @Test
    void mapsReservationUnavailableToConflict() {
        var response = handler.handleReservationUnavailable(
                new ReservationUnavailableException("Horario indisponivel para reserva."),
                request("/api/v1/reservations")
        );

        assertThat(response.getStatusCode().value()).isEqualTo(409);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error()).isEqualTo("Conflict");
        assertThat(response.getBody().message()).isEqualTo("Horario indisponivel para reserva.");
    }

    @Test
    void mapsValidationParameterErrorToBadRequest() {
        var response = handler.handleInvalidRequestParameter(
                new IllegalArgumentException("invalid"),
                request("/api/v1/reservations/available-days")
        );

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Parametro de requisicao invalido.");
    }

    @Test
    void mapsUnexpectedErrorToInternalServerErrorWithoutInternalDetails() {
        var response = handler.handleUnexpectedException(
                new RuntimeException("ORA-00001 stack trace details"),
                request("/api/v1/reservations")
        );

        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Erro interno inesperado.");
        assertThat(response.getBody().message()).doesNotContain("ORA-00001");
    }

    private HttpServletRequest request(String path) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn(path);
        return request;
    }
}
