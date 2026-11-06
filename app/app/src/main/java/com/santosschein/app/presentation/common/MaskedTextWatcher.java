package com.santosschein.app.presentation.common;

import android.text.Editable;
import android.text.TextWatcher;

import com.google.android.material.textfield.TextInputEditText;

public final class MaskedTextWatcher implements TextWatcher {

    private final TextInputEditText editText;
    private final String mask;
    private boolean updating;

    public MaskedTextWatcher(TextInputEditText editText, String mask) {
        this.editText = editText;
        this.mask = mask;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable editable) {
        if (updating) {
            return;
        }

        updating = true;
        String formatted = applyMask(onlyDigits(editable.toString()));
        editText.setText(formatted);
        editText.setSelection(formatted.length());
        updating = false;
    }

    private String applyMask(String digits) {
        StringBuilder builder = new StringBuilder();
        int digitIndex = 0;
        for (int i = 0; i < mask.length() && digitIndex < digits.length(); i++) {
            char maskChar = mask.charAt(i);
            if (maskChar == '#') {
                builder.append(digits.charAt(digitIndex));
                digitIndex++;
            } else {
                builder.append(maskChar);
            }
        }
        return builder.toString();
    }

    private String onlyDigits(String value) {
        return value == null ? "" : value.replaceAll("\\D", "");
    }
}
