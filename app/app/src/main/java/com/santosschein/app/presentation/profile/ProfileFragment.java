package com.santosschein.app.presentation.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.santosschein.app.MainActivity;
import com.santosschein.app.R;
import com.santosschein.app.core.error.ErrorType;
import com.santosschein.app.domain.model.User;
import com.santosschein.app.presentation.common.AppViewModelFactory;
import com.santosschein.app.presentation.common.PresentationFormatters;
import com.santosschein.app.presentation.navigation.AppDestination;

public final class ProfileFragment extends Fragment {

    private ProfileViewModel viewModel;
    private TextView nameTextView;
    private TextView birthDateTextView;
    private TextView documentTextView;
    private TextView emailTextView;
    private CircularProgressIndicator progressIndicator;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MainActivity activity = (MainActivity) requireActivity();
        viewModel = new ViewModelProvider(
                this,
                new AppViewModelFactory(activity.getAppContainer())
        ).get(ProfileViewModel.class);

        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(null);

        nameTextView = view.findViewById(R.id.nameTextView);
        birthDateTextView = view.findViewById(R.id.birthDateTextView);
        documentTextView = view.findViewById(R.id.documentTextView);
        emailTextView = view.findViewById(R.id.emailTextView);
        progressIndicator = view.findViewById(R.id.progressIndicator);
        View logoutButton = view.findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(v -> viewModel.logout());

        viewModel.getLogoutState().observe(getViewLifecycleOwner(), state -> {
            boolean loading = state.isLoading();
            logoutButton.setEnabled(!loading);
            if (loading) {
                progressIndicator.setVisibility(View.VISIBLE);
            } else if (state.isSuccess() || state.isError()) {
                // A sessao local ja foi apagada nos dois casos. No erro, avisa que o servidor
                // nao confirmou a saida (o historico fica sem logout ate a expiracao).
                if (state.isError() && state.getErrorMessage() != null) {
                    Toast.makeText(activity.getApplicationContext(), state.getErrorMessage(), Toast.LENGTH_LONG).show();
                }
                activity.navigateTo(AppDestination.LOGIN);
            }
        });

        viewModel.getProfileState().observe(getViewLifecycleOwner(), state -> {
            progressIndicator.setVisibility(state.isLoading() ? View.VISIBLE : View.GONE);
            if (state.isSuccess()) {
                renderProfile(state.getData());
            } else if (state.isError()) {
                if (state.getError() != null && state.getError().getType() == ErrorType.UNAUTHORIZED) {
                    activity.navigateTo(AppDestination.LOGIN);
                    return;
                }
                Snackbar.make(view, state.getErrorMessage(), Snackbar.LENGTH_LONG).show();
            }
        });
        viewModel.load();
    }

    private void renderProfile(User user) {
        if (user == null) {
            return;
        }
        nameTextView.setText(user.getFirstName() + " " + user.getLastName());
        birthDateTextView.setText("Nascimento: " + PresentationFormatters.date(user.getBirthDate()));
        documentTextView.setText("CPF: " + PresentationFormatters.cpf(user.getDocument()));
        emailTextView.setText(user.getEmail());
    }
}
