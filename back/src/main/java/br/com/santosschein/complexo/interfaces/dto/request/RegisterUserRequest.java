package br.com.santosschein.complexo.interfaces.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

public record RegisterUserRequest(
        @NotBlank
        String firstName,

        @NotBlank
        String lastName,

        @NotNull
        LocalDate birthDate,

        @NotBlank
        String document,

        @NotBlank
        @Email
        String email,

        // RF01: no minimo 8 caracteres, com pelo menos uma letra, um numero e um simbolo
        // (mesma regra validada no app em ValidationUtils.isWeakPassword).
        @NotBlank
        @Pattern(
                regexp = "^(?=.*\\p{L})(?=.*\\p{Nd})(?=.*[^\\p{L}\\p{Nd}]).{8,}$",
                message = "A senha deve ter no minimo 8 caracteres, letras, numeros e simbolos."
        )
        String password
) {

    /**
     * Remove espacos nas pontas antes da validacao (@Email recusaria " ana@email.com ").
     * A normalizacao completa (minusculas, CPF so com digitos) fica no caso de uso.
     */
    public RegisterUserRequest {
        email = email == null ? null : email.trim();
        document = document == null ? null : document.trim();
    }
}
