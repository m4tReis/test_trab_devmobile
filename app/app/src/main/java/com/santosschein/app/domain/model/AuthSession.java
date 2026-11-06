package com.santosschein.app.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public final class AuthSession {

    private final String jwtToken;
    private final User user;
}
