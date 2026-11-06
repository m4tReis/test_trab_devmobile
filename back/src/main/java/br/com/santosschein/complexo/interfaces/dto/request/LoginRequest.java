package br.com.santosschein.complexo.interfaces.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank
        @Email
        String email,

        @NotBlank
        String password
) {

    /**
     * Remove espacos nas pontas do e-mail antes da validacao.
     */
    public LoginRequest {
        email = email == null ? null : email.trim();
    }
}
