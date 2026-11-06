package com.santosschein.app.data.repository;

import com.santosschein.app.core.network.HttpErrorMapper;
import com.santosschein.app.core.session.SessionManager;
import com.santosschein.app.core.util.Result;
import com.santosschein.app.data.remote.api.ReservationApi;
import com.santosschein.app.data.remote.dto.AvailableDaysResponseDto;
import com.santosschein.app.data.remote.dto.AvailableSlotsResponseDto;
import com.santosschein.app.data.remote.dto.CreateReservationRequestDto;
import com.santosschein.app.data.remote.dto.ReservationDto;
import com.santosschein.app.data.remote.dto.ReservationListResponseDto;
import com.santosschein.app.data.remote.dto.ReservationSlotDto;
import com.santosschein.app.data.remote.mapper.ReservationMapper;
import com.santosschein.app.data.remote.mapper.ReservationSlotMapper;
import com.santosschein.app.domain.model.Reservation;
import com.santosschein.app.domain.model.ReservationSlot;
import com.santosschein.app.domain.repository.ReservationRepository;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.RequiredArgsConstructor;
import retrofit2.Response;

@RequiredArgsConstructor
public final class RemoteReservationRepository implements ReservationRepository {

    private static final String CREATE_CONFLICT_MESSAGE = "Esse horario nao esta mais disponivel.";
    private static final String CANCEL_CONFLICT_MESSAGE = "Nao e possivel cancelar com menos de 24 horas de antecedencia.";

    private final ReservationApi reservationApi;
    private final SessionManager sessionManager;
    private final HttpErrorMapper errorMapper;
    private final ReservationMapper reservationMapper = new ReservationMapper();
    private final ReservationSlotMapper slotMapper = new ReservationSlotMapper();

    @Override
    public Result<Reservation> getNextReservation() {
        try {
            Response<ReservationDto> response = reservationApi.getNextReservation().execute();
            if (response.code() == 204) {
                return Result.success(null);
            }
            if (!response.isSuccessful() || response.body() == null) {
                return error(response, null);
            }
            return Result.success(reservationMapper.toDomain(response.body()));
        } catch (IOException exception) {
            return Result.failure(errorMapper.fromNetworkError(exception));
        } catch (RuntimeException exception) {
            return Result.failure(errorMapper.unexpected(exception));
        }
    }

    @Override
    public Result<List<LocalDate>> getAvailableDays(int year, int month) {
        try {
            Response<AvailableDaysResponseDto> response = reservationApi.getAvailableDays(year, month).execute();
            if (!response.isSuccessful() || response.body() == null) {
                return error(response, null);
            }

            List<String> daysDto = response.body().getAvailableDays();
            if (daysDto == null) {
                return Result.success(Collections.emptyList());
            }

            List<LocalDate> days = new ArrayList<>();
            for (String day : daysDto) {
                days.add(LocalDate.parse(day));
            }
            return Result.success(days);
        } catch (IOException exception) {
            return Result.failure(errorMapper.fromNetworkError(exception));
        } catch (RuntimeException exception) {
            return Result.failure(errorMapper.unexpected(exception));
        }
    }

    @Override
    public Result<List<ReservationSlot>> getAvailableSlots(LocalDate date) {
        try {
            Response<AvailableSlotsResponseDto> response = reservationApi.getAvailableSlots(date.toString()).execute();
            if (!response.isSuccessful() || response.body() == null) {
                return error(response, null);
            }

            List<ReservationSlotDto> slotsDto = response.body().getSlots();
            if (slotsDto == null) {
                return Result.success(Collections.emptyList());
            }

            List<ReservationSlot> slots = new ArrayList<>();
            for (ReservationSlotDto slotDto : slotsDto) {
                slots.add(slotMapper.toDomain(slotDto));
            }
            return Result.success(slots);
        } catch (IOException exception) {
            return Result.failure(errorMapper.fromNetworkError(exception));
        } catch (RuntimeException exception) {
            return Result.failure(errorMapper.unexpected(exception));
        }
    }

    @Override
    public Result<Reservation> createReservation(LocalDate date, String slotId) {
        try {
            Response<ReservationDto> response = reservationApi
                    .createReservation(new CreateReservationRequestDto(date.toString(), slotId))
                    .execute();
            if (!response.isSuccessful() || response.body() == null) {
                return error(response, CREATE_CONFLICT_MESSAGE);
            }
            return Result.success(reservationMapper.toDomain(response.body()));
        } catch (IOException exception) {
            return Result.failure(errorMapper.fromNetworkError(exception));
        } catch (RuntimeException exception) {
            return Result.failure(errorMapper.unexpected(exception));
        }
    }

    @Override
    public Result<List<Reservation>> getUpcomingReservations() {
        return getReservationsByStatus("upcoming");
    }

    @Override
    public Result<List<Reservation>> getPastReservations() {
        return getReservationsByStatus("past");
    }

    @Override
    public Result<Void> cancelReservation(String reservationId) {
        try {
            Response<Void> response = reservationApi.cancelReservation(reservationId).execute();
            if (!response.isSuccessful()) {
                return error(response, CANCEL_CONFLICT_MESSAGE);
            }
            return Result.success(null);
        } catch (IOException exception) {
            return Result.failure(errorMapper.fromNetworkError(exception));
        } catch (RuntimeException exception) {
            return Result.failure(errorMapper.unexpected(exception));
        }
    }

    private Result<List<Reservation>> getReservationsByStatus(String status) {
        try {
            Response<ReservationListResponseDto> response = reservationApi.getReservations(status).execute();
            if (!response.isSuccessful() || response.body() == null) {
                return error(response, null);
            }

            List<ReservationDto> reservationsDto = response.body().getReservations();
            if (reservationsDto == null) {
                return Result.success(Collections.emptyList());
            }

            List<Reservation> reservations = new ArrayList<>();
            for (ReservationDto dto : reservationsDto) {
                reservations.add(reservationMapper.toDomain(dto));
            }
            return Result.success(reservations);
        } catch (IOException exception) {
            return Result.failure(errorMapper.fromNetworkError(exception));
        } catch (RuntimeException exception) {
            return Result.failure(errorMapper.unexpected(exception));
        }
    }

    private <T> Result<T> error(Response<?> response, String conflictMessage) {
        if (response.code() == 401) {
            sessionManager.clearSession();
        }
        return Result.failure(errorMapper.fromHttpCode(response.code(), conflictMessage));
    }
}
