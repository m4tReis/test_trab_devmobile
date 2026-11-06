package com.santosschein.app.presentation.reservation.create;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.santosschein.app.MainActivity;
import com.santosschein.app.R;
import com.santosschein.app.domain.model.ReservationSlot;
import com.santosschein.app.presentation.common.AppViewModelFactory;
import com.santosschein.app.presentation.common.PresentationFormatters;
import com.santosschein.app.core.error.ErrorType;
import com.santosschein.app.presentation.navigation.AppDestination;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class CreateReservationFragment extends Fragment {

    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("MMMM yyyy", new Locale("pt", "BR"));

    private CreateReservationViewModel viewModel;
    private CalendarDayAdapter calendarDayAdapter;
    private SlotAdapter slotAdapter;
    private TextView monthTextView;
    private TextView emptyTextView;
    private TextView summaryTextView;
    private MaterialCardView summaryCard;
    private MaterialButton confirmButton;
    private CircularProgressIndicator progressIndicator;
    private YearMonth visibleMonth = YearMonth.now();

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_create_reservation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MainActivity activity = (MainActivity) requireActivity();
        viewModel = new ViewModelProvider(
                this,
                new AppViewModelFactory(activity.getAppContainer())
        ).get(CreateReservationViewModel.class);

        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(null);

        monthTextView = view.findViewById(R.id.monthTextView);
        emptyTextView = view.findViewById(R.id.emptyTextView);
        summaryTextView = view.findViewById(R.id.summaryTextView);
        summaryCard = view.findViewById(R.id.summaryCard);
        confirmButton = view.findViewById(R.id.confirmButton);
        progressIndicator = view.findViewById(R.id.progressIndicator);

        RecyclerView calendarRecyclerView = view.findViewById(R.id.calendarRecyclerView);
        calendarDayAdapter = new CalendarDayAdapter(day -> viewModel.selectDate(day.getDate()));
        calendarRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 7));
        calendarRecyclerView.setAdapter(calendarDayAdapter);

        RecyclerView slotsRecyclerView = view.findViewById(R.id.slotsRecyclerView);
        slotAdapter = new SlotAdapter(slot -> viewModel.selectSlot(slot));
        slotsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        slotsRecyclerView.setAdapter(slotAdapter);

        view.findViewById(R.id.previousMonthButton).setOnClickListener(v -> {
            visibleMonth = visibleMonth.minusMonths(1);
            viewModel.loadMonth(visibleMonth);
        });
        view.findViewById(R.id.nextMonthButton).setOnClickListener(v -> {
            visibleMonth = visibleMonth.plusMonths(1);
            viewModel.loadMonth(visibleMonth);
        });
        confirmButton.setOnClickListener(v -> viewModel.confirmReservation());

        viewModel.getState().observe(getViewLifecycleOwner(), state -> {
            progressIndicator.setVisibility(state.isLoading() ? View.VISIBLE : View.GONE);
            if (state.isSuccess() && state.getData() != null) {
                render(state.getData());
            } else if (state.isEmpty()) {
                emptyTextView.setText(state.getMessage());
                emptyTextView.setVisibility(View.VISIBLE);
                if (state.getData() != null) {
                    render(state.getData());
                }
            } else if (state.isError()) {
                if (state.getError() != null && state.getError().getType() == ErrorType.UNAUTHORIZED) {
                    activity.navigateTo(AppDestination.LOGIN);
                    return;
                }
                Snackbar.make(view, state.getErrorMessage(), Snackbar.LENGTH_LONG).show();
            }
        });

        viewModel.getCreationState().observe(getViewLifecycleOwner(), state -> {
            if (state.isLoading()) {
                progressIndicator.setVisibility(View.VISIBLE);
                confirmButton.setEnabled(false);
            } else if (state.isSuccess()) {
                progressIndicator.setVisibility(View.GONE);
                Snackbar.make(view, "Reserva criada com sucesso.", Snackbar.LENGTH_LONG).show();
                activity.navigateTo(AppDestination.HOME);
            } else if (state.isError()) {
                progressIndicator.setVisibility(View.GONE);
                confirmButton.setEnabled(true);
                if (state.getError() != null && state.getError().getType() == ErrorType.UNAUTHORIZED) {
                    activity.navigateTo(AppDestination.LOGIN);
                    return;
                }
                String message = state.getErrorMessage();
                if (message != null && message.contains("horario")) {
                    message = "Esse horário acabou de ser reservado. Escolha outro horário.";
                }
                Snackbar.make(view, message, Snackbar.LENGTH_LONG).show();
            }
        });

        viewModel.loadMonth(visibleMonth);
    }

    private void render(CreateReservationUiModel model) {
        visibleMonth = model.getVisibleMonth();
        monthTextView.setText(MONTH_FORMAT.format(visibleMonth));
        calendarDayAdapter.submitList(buildCalendarDays(model));
        slotAdapter.submitList(model.getAvailableSlots(), model.getSelectedSlot());

        boolean hasSlots = model.getAvailableSlots() != null && !model.getAvailableSlots().isEmpty();
        emptyTextView.setVisibility(hasSlots || model.getSelectedDate() == null ? View.GONE : View.VISIBLE);
        if (!hasSlots && model.getSelectedDate() != null) {
            emptyTextView.setText("Nenhum horário disponível para esta data.");
        }

        ReservationSlot selectedSlot = model.getSelectedSlot();
        if (selectedSlot == null || model.getSelectedDate() == null) {
            summaryCard.setVisibility(View.GONE);
            confirmButton.setEnabled(false);
            return;
        }

        summaryCard.setVisibility(View.VISIBLE);
        confirmButton.setEnabled(true);
        summaryTextView.setText("Campo 1\n"
                + PresentationFormatters.date(model.getSelectedDate())
                + "\n"
                + PresentationFormatters.time(selectedSlot.getStartTime())
                + " - "
                + PresentationFormatters.time(selectedSlot.getEndTime())
                + "\n"
                + PresentationFormatters.money(selectedSlot.getAmount()));
    }

    private List<CalendarDay> buildCalendarDays(CreateReservationUiModel model) {
        List<CalendarDay> days = new ArrayList<>();
        LocalDate firstDay = model.getVisibleMonth().atDay(1);
        int offset = firstDay.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue();
        LocalDate cursor = firstDay.minusDays(offset);
        for (int i = 0; i < 42; i++) {
            boolean inMonth = YearMonth.from(cursor).equals(model.getVisibleMonth());
            boolean available = inMonth && !cursor.isBefore(LocalDate.now()) && model.getAvailableDays().contains(cursor);
            boolean selected = cursor.equals(model.getSelectedDate());
            days.add(new CalendarDay(cursor, inMonth, available, selected));
            cursor = cursor.plusDays(1);
        }
        return days;
    }
}
