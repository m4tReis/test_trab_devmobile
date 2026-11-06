package br.com.santosschein.complexo.infrastructure.persistence.repository;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class JpaReservationSlotRepositoryTest {

    @Test
    void availabilityQueriesOnlyBlockActiveReservations() throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/br/com/santosschein/complexo/infrastructure/persistence/repository/JpaReservationSlotRepository.java"
        ));

        assertThat(source).contains("TO_CHAR(available_days.day_value, 'YYYY-MM-DD')");
        assertThat(source).contains("r.status = 'ACTIVE'");
        assertThat(source).doesNotContain("r.status <> 'CANCELED'");
    }

    @Test
    void firstAvailableEquivalentSlotQueryChoosesFirstActiveAvailableField() throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/br/com/santosschein/complexo/infrastructure/persistence/repository/JpaReservationSlotRepository.java"
        ));

        assertThat(source).contains("findFirstAvailableEquivalentSlot");
        assertThat(source).contains("candidate_sf.active = 1");
        assertThat(source).contains("ORDER BY candidate_sf.id ASC, candidate_sf.name ASC");
        assertThat(source).contains("FETCH FIRST 1 ROWS ONLY");
    }
}
