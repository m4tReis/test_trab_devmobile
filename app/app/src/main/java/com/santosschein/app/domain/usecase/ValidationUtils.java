package com.santosschein.app.domain.usecase;

import com.santosschein.app.core.error.AppError;
import com.santosschein.app.core.error.ErrorType;
import com.santosschein.app.core.util.Result;

final class ValidationUtils {

    private ValidationUtils() {
    }

    static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    static boolean isInvalidEmail(String email) {
        return isBlank(email) || !email.contains("@");
    }

    static boolean isInvalidCpf(String document) {
        return document == null || document.replaceAll("\\D", "").length() != 11;
    }

    static boolean isWeakPassword(String password) {
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

    static <T> Result<T> validationError(String message) {
        return Result.failure(new AppError(ErrorType.VALIDATION, message));
    }
}
