package com.santosschein.app.presentation.auth.login;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.santosschein.app.MainActivity;
import com.santosschein.app.R;
import com.santosschein.app.presentation.common.AppViewModelFactory;
import com.santosschein.app.presentation.common.UiState;
import com.santosschein.app.presentation.navigation.AppDestination;

public final class LoginFragment extends Fragment {

    private LoginViewModel viewModel;
    private TextInputLayout emailInputLayout;
    private TextInputLayout passwordInputLayout;
    private TextInputEditText emailEditText;
    private TextInputEditText passwordEditText;
    private MaterialButton loginButton;
    private CircularProgressIndicator progressIndicator;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MainActivity activity = (MainActivity) requireActivity();
        viewModel = new ViewModelProvider(
                this,
                new AppViewModelFactory(activity.getAppContainer())
        ).get(LoginViewModel.class);

        emailInputLayout = view.findViewById(R.id.emailInputLayout);
        passwordInputLayout = view.findViewById(R.id.passwordInputLayout);
        emailEditText = view.findViewById(R.id.emailEditText);
        passwordEditText = view.findViewById(R.id.passwordEditText);
        loginButton = view.findViewById(R.id.loginButton);
        progressIndicator = view.findViewById(R.id.progressIndicator);

        loginButton.setOnClickListener(v -> submitLogin());

        // Parte 3: o token salvo foi recusado pelo servidor (sessao vencida ou encerrada).
        if (activity.getAppContainer().getSessionManager().consumeSessionExpiredNotice()) {
            Snackbar.make(view, "Sessão expirada. Entre novamente.", Snackbar.LENGTH_LONG).show();
        }
        view.findViewById(R.id.registerButton).setOnClickListener(v -> activity.navigateTo(AppDestination.REGISTER));

        viewModel.getLoginState().observe(getViewLifecycleOwner(), state -> {
            boolean loading = state.isLoading();
            progressIndicator.setVisibility(loading ? View.VISIBLE : View.GONE);
            loginButton.setEnabled(!loading);

            if (state.isSuccess()) {
                activity.navigateTo(AppDestination.HOME);
            } else if (state.isError()) {
                Snackbar.make(view, state.getErrorMessage(), Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void submitLogin() {
        emailInputLayout.setError(null);
        passwordInputLayout.setError(null);

        String email = textOf(emailEditText);
        String password = textOf(passwordEditText);
        boolean valid = true;
        if (email.trim().isEmpty() || !email.contains("@")) {
            emailInputLayout.setError("Informe um e-mail valido.");
            valid = false;
        }
        if (password.trim().isEmpty()) {
            passwordInputLayout.setError("Informe a senha.");
            valid = false;
        }
        if (valid) {
            viewModel.login(email, password);
        }
    }

    private String textOf(TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString() : "";
    }
}
