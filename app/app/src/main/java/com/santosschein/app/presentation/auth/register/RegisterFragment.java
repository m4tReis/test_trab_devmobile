package com.santosschein.app.presentation.auth.register;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.santosschein.app.MainActivity;
import com.santosschein.app.R;
import com.santosschein.app.presentation.common.AppViewModelFactory;
import com.santosschein.app.presentation.common.MaskedTextWatcher;
import com.santosschein.app.presentation.common.PresentationFormatters;

public final class RegisterFragment extends Fragment {

    private RegisterViewModel viewModel;
    private TextInputLayout firstNameInputLayout;
    private TextInputLayout lastNameInputLayout;
    private TextInputLayout birthDateInputLayout;
    private TextInputLayout documentInputLayout;
    private TextInputLayout emailInputLayout;
    private TextInputLayout passwordInputLayout;
    private TextInputEditText firstNameEditText;
    private TextInputEditText lastNameEditText;
    private TextInputEditText birthDateEditText;
    private TextInputEditText documentEditText;
    private TextInputEditText emailEditText;
    private TextInputEditText passwordEditText;
    private MaterialButton registerButton;
    private CircularProgressIndicator progressIndicator;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MainActivity activity = (MainActivity) requireActivity();
        viewModel = new ViewModelProvider(
                this,
                new AppViewModelFactory(activity.getAppContainer())
        ).get(RegisterViewModel.class);

        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> activity.navigateBack());

        firstNameInputLayout = view.findViewById(R.id.firstNameInputLayout);
        lastNameInputLayout = view.findViewById(R.id.lastNameInputLayout);
        birthDateInputLayout = view.findViewById(R.id.birthDateInputLayout);
        documentInputLayout = view.findViewById(R.id.documentInputLayout);
        emailInputLayout = view.findViewById(R.id.emailInputLayout);
        passwordInputLayout = view.findViewById(R.id.passwordInputLayout);
        firstNameEditText = view.findViewById(R.id.firstNameEditText);
        lastNameEditText = view.findViewById(R.id.lastNameEditText);
        birthDateEditText = view.findViewById(R.id.birthDateEditText);
        documentEditText = view.findViewById(R.id.documentEditText);
        emailEditText = view.findViewById(R.id.emailEditText);
        passwordEditText = view.findViewById(R.id.passwordEditText);
        registerButton = view.findViewById(R.id.registerButton);
        progressIndicator = view.findViewById(R.id.progressIndicator);

        birthDateEditText.addTextChangedListener(new MaskedTextWatcher(birthDateEditText, "##/##/####"));
        documentEditText.addTextChangedListener(new MaskedTextWatcher(documentEditText, "###.###.###-##"));
        registerButton.setOnClickListener(v -> submitRegister());
        viewModel.getRegisterState().observe(getViewLifecycleOwner(), state -> {
            boolean loading = state.isLoading();
            progressIndicator.setVisibility(loading ? View.VISIBLE : View.GONE);
            registerButton.setEnabled(!loading);

            if (state.isSuccess()) {
                Snackbar.make(view, "Cadastro realizado. Entre com seus dados.", Snackbar.LENGTH_LONG).show();
                activity.navigateBack();
            } else if (state.isError()) {
                Snackbar.make(view, state.getErrorMessage(), Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void submitRegister() {
        clearErrors();
        boolean valid = true;
        String firstName = textOf(firstNameEditText);
        String lastName = textOf(lastNameEditText);
        String birthDate = textOf(birthDateEditText);
        String document = textOf(documentEditText);
        String email = textOf(emailEditText);
        String password = textOf(passwordEditText);

        if (firstName.trim().isEmpty()) {
            firstNameInputLayout.setError("Nome obrigatorio.");
            valid = false;
        }
        if (lastName.trim().isEmpty()) {
            lastNameInputLayout.setError("Sobrenome obrigatorio.");
            valid = false;
        }
        if (birthDate.trim().isEmpty()) {
            birthDateInputLayout.setError("Data de nascimento obrigatoria.");
            valid = false;
        } else if (PresentationFormatters.parseBrazilianDate(birthDate) == null) {
            birthDateInputLayout.setError("Informe uma data valida no formato DD/MM/AAAA.");
            valid = false;
        }
        if (PresentationFormatters.onlyDigits(document).length() != 11) {
            documentInputLayout.setError("Informe um CPF completo.");
            valid = false;
        }
        if (email.trim().isEmpty() || !email.contains("@")) {
            emailInputLayout.setError("Informe um e-mail valido.");
            valid = false;
        }
        if (isWeakPassword(password)) {
            passwordInputLayout.setError("Use 8+ caracteres com letras, numeros e simbolos.");
            valid = false;
        }
        if (valid) {
            viewModel.register(firstName, lastName, birthDate, document, email, password);
        }
    }

    private void clearErrors() {
        firstNameInputLayout.setError(null);
        lastNameInputLayout.setError(null);
        birthDateInputLayout.setError(null);
        documentInputLayout.setError(null);
        emailInputLayout.setError(null);
        passwordInputLayout.setError(null);
    }

    private String textOf(TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString() : "";
    }

    private boolean isWeakPassword(String password) {
        if (password == null || password.length() < 8) {
            return true;
        }
        boolean hasLetter = false;
        boolean hasNumber = false;
        boolean hasSymbol = false;
        for (int i = 0; i < password.length(); i++) {
            char current = password.charAt(i);
            if (Character.isLetter(current)) {
                hasLetter = true;
            } else if (Character.isDigit(current)) {
                hasNumber = true;
            } else {
                hasSymbol = true;
            }
        }
        return !hasLetter || !hasNumber || !hasSymbol;
    }
}
