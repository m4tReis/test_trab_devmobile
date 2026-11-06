package br.com.santosschein.complexo;

import br.com.santosschein.complexo.domain.model.ReservationStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationStatusTest {

    @Test
    void hasInitialReservationStatuses() {
        assertThat(ReservationStatus.values())
                .containsExactly(ReservationStatus.ACTIVE, ReservationStatus.CANCELED, ReservationStatus.COMPLETED);
    }
}
