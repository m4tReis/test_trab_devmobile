package br.com.santosschein.complexo.infrastructure.persistence.migration;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationMigrationTest {

    @Test
    void schemaPreventsDuplicateActiveReservationForSameFieldDateAndSlot() throws Exception {
        String schema = Files.readString(Path.of("src/main/resources/db/migration/V1__create_initial_schema.sql"));
        String constraints = Files.readString(Path.of("src/main/resources/db/migration/V2__create_constraints.sql"));

        assertThat(schema).contains("active_field_id NUMBER");
        assertThat(schema).contains("CASE WHEN status = 'ACTIVE' THEN field_id ELSE NULL END");
        assertThat(schema).contains("active_reservation_date DATE");
        assertThat(schema).contains("active_slot_id NUMBER");
        assertThat(constraints).contains("uk_reservations_active_slot");
        assertThat(constraints).contains("UNIQUE (active_field_id, active_reservation_date, active_slot_id)");
    }
}
