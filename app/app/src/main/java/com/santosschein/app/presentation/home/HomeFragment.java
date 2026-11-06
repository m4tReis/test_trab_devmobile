package com.santosschein.app.presentation.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.santosschein.app.MainActivity;
import com.santosschein.app.R;
import com.santosschein.app.core.error.ErrorType;
import com.santosschein.app.domain.model.Reservation;
import com.santosschein.app.presentation.common.AppViewModelFactory;
import com.santosschein.app.presentation.common.PresentationFormatters;
import com.santosschein.app.presentation.navigation.AppDestination;

public final class HomeFragment extends Fragment {

    private HomeViewModel viewModel;
    private TextView greetingTextView;
    private MaterialCardView nextReservationCard;
    private TextView nextReservationTitleTextView;
    private TextView nextReservationDetailsTextView;
    private TextView emptyNextReservationTextView;
    private CircularProgressIndicator progressIndicator;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MainActivity activity = (MainActivity) requireActivity();
        viewModel = new ViewModelProvider(
                this,
                new AppViewModelFactory(activity.getAppContainer())
        ).get(HomeViewModel.class);

        greetingTextView = view.findViewById(R.id.greetingTextView);
        nextReservationCard = view.findViewById(R.id.nextReservationCard);
        nextReservationTitleTextView = view.findViewById(R.id.nextReservationTitleTextView);
        nextReservationDetailsTextView = view.findViewById(R.id.nextReservationDetailsTextView);
        emptyNextReservationTextView = view.findViewById(R.id.emptyNextReservationTextView);
        progressIndicator = view.findViewById(R.id.progressIndicator);

        view.findViewById(R.id.createReservationButton)
                .setOnClickListener(v -> activity.navigateTo(AppDestination.RESERVATION_CREATE));

        viewModel.getHomeState().observe(getViewLifecycleOwner(), state -> {
            progressIndicator.setVisibility(state.isLoading() ? View.VISIBLE : View.GONE);
            if (state.isSuccess() && state.getData() != null) {
                renderHome(state.getData());
            } else if (state.isError()) {
                if (state.getError() != null && state.getError().getType() == ErrorType.UNAUTHORIZED) {
                    activity.navigateTo(AppDestination.LOGIN);
                    return;
                }
                Snackbar.make(view, state.getErrorMessage(), Snackbar.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.load();
    }

    private void renderHome(HomeUiModel home) {
        String firstName = home.getUser() != null ? home.getUser().getFirstName() : "";
        greetingTextView.setText(firstName.trim().isEmpty() ? "Olá" : "Olá, " + firstName);

        Reservation nextReservation = home.getNextReservation();
        if (nextReservation == null) {
            nextReservationCard.setVisibility(View.GONE);
            emptyNextReservationTextView.setVisibility(View.VISIBLE);
            return;
        }

        nextReservationCard.setVisibility(View.VISIBLE);
        emptyNextReservationTextView.setVisibility(View.GONE);
        nextReservationTitleTextView.setText(nextReservation.getFieldName());
        nextReservationDetailsTextView.setText(PresentationFormatters.date(nextReservation.getDate())
                + " • "
                + PresentationFormatters.time(nextReservation.getStartTime())
                + " - "
                + PresentationFormatters.time(nextReservation.getEndTime())
                + " • "
                + PresentationFormatters.money(nextReservation.getAmount()));
    }
}
