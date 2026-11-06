package com.santosschein.app.data.remote.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public final class LoginRequestDto {

    private final String email;
    private final String password;
}
