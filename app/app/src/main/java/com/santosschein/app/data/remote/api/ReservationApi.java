package com.santosschein.app.data.remote.api;

import com.santosschein.app.data.remote.dto.AvailableDaysResponseDto;
import com.santosschein.app.data.remote.dto.AvailableSlotsResponseDto;
import com.santosschein.app.data.remote.dto.CreateReservationRequestDto;
import com.santosschein.app.data.remote.dto.ReservationDto;
import com.santosschein.app.data.remote.dto.ReservationListResponseDto;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ReservationApi {

    @GET("api/v1/users/me/reservations/next")
    Call<ReservationDto> getNextReservation();

    @GET("api/v1/reservations/available-days")
    Call<AvailableDaysResponseDto> getAvailableDays(@Query("year") int year, @Query("month") int month);

    @GET("api/v1/reservations/available-slots")
    Call<AvailableSlotsResponseDto> getAvailableSlots(@Query("date") String date);

    @POST("api/v1/reservations")
    Call<ReservationDto> createReservation(@Body CreateReservationRequestDto request);

    @GET("api/v1/users/me/reservations")
    Call<ReservationListResponseDto> getReservations(@Query("status") String status);

    @DELETE("api/v1/users/me/reservations/{reservationId}")
    Call<Void> cancelReservation(@Path("reservationId") String reservationId);
}
