package com.santosschein.app;

import com.santosschein.app.domain.model.ReservationStatus;
import com.santosschein.app.presentation.common.PresentationFormatters;

import org.junit.Test;

import java.time.LocalDate;

import static org.junit.Assert.assertEquals;

public class PresentationFormattersTest {

    @Test
    public void cpfFormatsElevenDigits() {
        assertEquals("123.456.789-00", PresentationFormatters.cpf("12345678900"));
    }

    @Test
    public void statusIsShownInPortuguese() {
        assertEquals("Ativa", PresentationFormatters.status(ReservationStatus.ACTIVE));
        assertEquals("Cancelada", PresentationFormatters.status(ReservationStatus.CANCELLED));
        assertEquals("Concluída", PresentationFormatters.status(ReservationStatus.COMPLETED));
        assertEquals("", PresentationFormatters.status(null));
    }

    @Test
    public void parseBrazilianDateReturnsLocalDate() {
        assertEquals(LocalDate.of(1995, 4, 20), PresentationFormatters.parseBrazilianDate("20/04/1995"));
    }
}
