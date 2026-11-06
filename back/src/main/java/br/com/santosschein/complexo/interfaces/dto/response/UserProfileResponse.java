package br.com.santosschein.complexo.interfaces.dto.response;

import java.time.LocalDate;

public record UserProfileResponse(
        String id,
        String firstName,
        String lastName,
        LocalDate birthDate,
        String document,
        String email
) {
}
