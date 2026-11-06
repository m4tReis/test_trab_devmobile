package br.com.santosschein.complexo.interfaces.controller;

import br.com.santosschein.complexo.application.usecase.CancelReservationUseCase;
import br.com.santosschein.complexo.application.usecase.GetNextReservationUseCase;
import br.com.santosschein.complexo.application.usecase.ListUserReservationsUseCase;
import br.com.santosschein.complexo.security.AuthenticatedUser;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MeReservationsControllerTest {

    @Test
    void returnsNoContentWhenThereIsNoNextReservation() {
        GetNextReservationUseCase getNextReservationUseCase = mock(GetNextReservationUseCase.class);
        MeReservationsController controller = new MeReservationsController(
                getNextReservationUseCase,
                mock(ListUserReservationsUseCase.class),
                mock(CancelReservationUseCase.class)
        );
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(99L, "joao@email.com", "$2a$hash");
        when(getNextReservationUseCase.execute(99L)).thenReturn(Optional.empty());

        var response = controller.next(authenticatedUser);

        assertThat(response.getStatusCode().value()).isEqualTo(204);
        assertThat(response.getBody()).isNull();
    }
}
