package com.santosschein.app.data.remote.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public final class RegisterUserRequestDto {

    private final String firstName;
    private final String lastName;
    private final String birthDate;
    private final String document;
    private final String email;
    private final String password;
}
