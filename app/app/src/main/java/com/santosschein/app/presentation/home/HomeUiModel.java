package com.santosschein.app.presentation.home;

import com.santosschein.app.domain.model.Reservation;
import com.santosschein.app.domain.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public final class HomeUiModel {

    private final User user;
    private final Reservation nextReservation;
}
