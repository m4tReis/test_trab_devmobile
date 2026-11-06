package com.santosschein.app.presentation.reservation.list;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.santosschein.app.MainActivity;
import com.santosschein.app.R;
import com.santosschein.app.core.error.ErrorType;
import com.santosschein.app.domain.model.Reservation;
import com.santosschein.app.presentation.common.AppViewModelFactory;
import com.santosschein.app.presentation.navigation.AppDestination;

public final class ReservationListFragment extends Fragment {

    public static final String MODE_UPCOMING = "upcoming";
    public static final String MODE_PAST = "past";
    private static final String ARG_MODE = "mode";
    private static final String ARG_SHOW_MODE_TOGGLE = "showModeToggle";

    private ReservationListViewModel viewModel;
    private ReservationAdapter adapter;
    private RecyclerView recyclerView;
    private TextView emptyTextView;
    private CircularProgressIndicator progressIndicator;
    private boolean pastMode;
    private boolean showModeToggle;

    public static ReservationListFragment newInstance(String mode, boolean showModeToggle) {
        ReservationListFragment fragment = new ReservationListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_MODE, mode);
        args.putBoolean(ARG_SHOW_MODE_TOGGLE, showModeToggle);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_reservation_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MainActivity activity = (MainActivity) requireActivity();
        pastMode = MODE_PAST.equals(getArguments() != null ? getArguments().getString(ARG_MODE) : MODE_UPCOMING);
        showModeToggle = getArguments() != null && getArguments().getBoolean(ARG_SHOW_MODE_TOGGLE);
        viewModel = new ViewModelProvider(
                this,
                new AppViewModelFactory(activity.getAppContainer())
        ).get(ReservationListViewModel.class);

        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle(showModeToggle ? "Reservas" : (pastMode ? "Reservas passadas" : "Reservas futuras"));
        if (showModeToggle) {
            toolbar.setNavigationIcon(null);
        } else {
            toolbar.setNavigationOnClickListener(v -> activity.navigateBack());
        }

        MaterialButtonToggleGroup modeToggleGroup = view.findViewById(R.id.modeToggleGroup);
        modeToggleGroup.setVisibility(showModeToggle ? View.VISIBLE : View.GONE);
        modeToggleGroup.check(pastMode ? R.id.pastToggleButton : R.id.upcomingToggleButton);

        recyclerView = view.findViewById(R.id.reservationsRecyclerView);
        emptyTextView = view.findViewById(R.id.emptyTextView);
        progressIndicator = view.findViewById(R.id.progressIndicator);
        adapter = new ReservationAdapter(!pastMode, this::confirmCancel);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
        modeToggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) {
                return;
            }
            pastMode = checkedId == R.id.pastToggleButton;
            adapter.setShowCancelButton(!pastMode);
            viewModel.load(pastMode);
        });

        viewModel.getReservationsState().observe(getViewLifecycleOwner(), state -> {
            progressIndicator.setVisibility(state.isLoading() ? View.VISIBLE : View.GONE);
            if (state.isSuccess()) {
                recyclerView.setVisibility(View.VISIBLE);
                emptyTextView.setVisibility(View.GONE);
                adapter.submitList(state.getData());
            } else if (state.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                emptyTextView.setText(state.getMessage());
                emptyTextView.setVisibility(View.VISIBLE);
            } else if (state.isError()) {
                if (state.getError() != null && state.getError().getType() == ErrorType.UNAUTHORIZED) {
                    activity.navigateTo(AppDestination.LOGIN);
                    return;
                }
                Snackbar.make(view, state.getErrorMessage(), Snackbar.LENGTH_LONG).show();
            }
        });

        viewModel.getCancelState().observe(getViewLifecycleOwner(), state -> {
            if (state.isSuccess()) {
                Snackbar.make(view, "Reserva cancelada.", Snackbar.LENGTH_SHORT).show();
            } else if (state.isError()) {
                Snackbar.make(view, state.getErrorMessage(), Snackbar.LENGTH_LONG).show();
            }
        });

        viewModel.load(pastMode);
    }

    private void confirmCancel(Reservation reservation) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Cancelar reserva")
                .setMessage("Deseja cancelar esta reserva?")
                .setNegativeButton("Manter", null)
                .setPositiveButton("Cancelar", (dialog, which) -> viewModel.cancel(reservation.getId()))
                .show();
    }
}
