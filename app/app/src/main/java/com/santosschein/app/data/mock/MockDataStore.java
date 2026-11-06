package com.santosschein.app.data.mock;

import com.santosschein.app.domain.model.Reservation;
import com.santosschein.app.domain.model.ReservationSlot;
import com.santosschein.app.domain.model.ReservationStatus;
import com.santosschein.app.domain.model.User;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MockDataStore {

    public static final String VALID_EMAIL = "usuario@teste.com";
    public static final String VALID_PASSWORD = "123456";
    public static final String MOCK_TOKEN = "mock-jwt-token";

    private final List<User> users = new ArrayList<>();
    /** Senhas das contas criadas no cadastro simulado (e-mail em minusculas -> senha). */
    private final Map<String, String> passwordsByEmail = new HashMap<>();
    private final List<Reservation> upcomingReservations = new ArrayList<>();
    private final List<Reservation> pastReservations = new ArrayList<>();
    private final Map<LocalDate, List<ReservationSlot>> slotsByDate = new HashMap<>();

    public MockDataStore() {
        User defaultUser = new User(
                "user_123",
                "Usuario",
                "Teste",
                LocalDate.of(1995, 4, 20),
                "12345678900",
                VALID_EMAIL
        );
        users.add(defaultUser);
        passwordsByEmail.put(VALID_EMAIL, VALID_PASSWORD);

        LocalDate firstDate = LocalDate.now().plusDays(7);
        LocalDate secondDate = firstDate.plusDays(1);
        LocalDate thirdDate = firstDate.plusDays(3);

        addSlots(firstDate);
        addSlots(secondDate);
        addSlots(thirdDate);

        upcomingReservations.add(new Reservation(
                "res_123",
                "Campo 1",
                firstDate,
                LocalTime.of(19, 0),
                LocalTime.of(20, 0),
                new BigDecimal("150.00"),
                ReservationStatus.ACTIVE
        ));

        pastReservations.add(new Reservation(
                "res_001",
                "Campo 2",
                LocalDate.now().minusDays(7),
                LocalTime.of(18, 0),
                LocalTime.of(19, 0),
                new BigDecimal("150.00"),
                ReservationStatus.COMPLETED
        ));
    }

    public List<User> getUsers() {
        return users;
    }

    public User getDefaultUser() {
        return users.get(0);
    }

    public Map<String, String> getPasswordsByEmail() {
        return passwordsByEmail;
    }

    public List<Reservation> getUpcomingReservations() {
        return upcomingReservations;
    }

    public List<Reservation> getPastReservations() {
        return pastReservations;
    }

    public Map<LocalDate, List<ReservationSlot>> getSlotsByDate() {
        return slotsByDate;
    }

    public void clearUpcomingReservations() {
        upcomingReservations.clear();
    }

    private void addSlots(LocalDate date) {
        List<ReservationSlot> slots = new ArrayList<>();
        slots.add(new ReservationSlot("slot_1", LocalTime.of(18, 0), LocalTime.of(19, 0), new BigDecimal("150.00")));
        slots.add(new ReservationSlot("slot_2", LocalTime.of(19, 0), LocalTime.of(20, 0), new BigDecimal("150.00")));
        slots.add(new ReservationSlot("slot_3", LocalTime.of(20, 0), LocalTime.of(21, 0), new BigDecimal("150.00")));
        slotsByDate.put(date, slots);
    }
}
