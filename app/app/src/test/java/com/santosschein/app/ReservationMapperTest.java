package com.santosschein.app;

import com.google.gson.Gson;
import com.santosschein.app.data.remote.dto.ReservationDto;
import com.santosschein.app.data.remote.mapper.ReservationMapper;
import com.santosschein.app.domain.model.Reservation;
import com.santosschein.app.domain.model.ReservationStatus;

import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.Assert.assertEquals;

public class ReservationMapperTest {

    private final Gson gson = new Gson();
    private final ReservationMapper mapper = new ReservationMapper();

    @Test
    public void mapsReservationReturnedByBackend() {
        Reservation reservation = mapper.toDomain(dtoWithStatus("ACTIVE"));

        assertEquals("res_10", reservation.getId());
        assertEquals("Campo 1", reservation.getFieldName());
        assertEquals(LocalDate.of(2026, 11, 20), reservation.getDate());
        assertEquals(LocalTime.of(19, 0), reservation.getStartTime());
        assertEquals(LocalTime.of(20, 0), reservation.getEndTime());
        assertEquals(new BigDecimal("180.00"), reservation.getAmount());
        assertEquals(ReservationStatus.ACTIVE, reservation.getStatus());
    }

    @Test
    public void mapsBackendCanceledStatusAsCancelled() {
        assertEquals(ReservationStatus.CANCELLED, mapper.toDomain(dtoWithStatus("CANCELED")).getStatus());
    }

    @Test
    public void mapsReportCancelledSpellingAsCancelled() {
        assertEquals(ReservationStatus.CANCELLED, mapper.toDomain(dtoWithStatus("CANCELLED")).getStatus());
    }

    @Test
    public void mapsCompletedStatus() {
        assertEquals(ReservationStatus.COMPLETED, mapper.toDomain(dtoWithStatus("COMPLETED")).getStatus());
    }

    private ReservationDto dtoWithStatus(String status) {
        String json = "{"
                + "\"id\":\"res_10\","
                + "\"fieldName\":\"Campo 1\","
                + "\"date\":\"2026-11-20\","
                + "\"startTime\":\"19:00\","
                + "\"endTime\":\"20:00\","
                + "\"price\":180.00,"
                + "\"status\":\"" + status + "\""
                + "}";
        return gson.fromJson(json, ReservationDto.class);
    }
}
