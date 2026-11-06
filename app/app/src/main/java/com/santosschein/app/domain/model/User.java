package com.santosschein.app.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public final class User {

    private final String id;
    private final String firstName;
    private final String lastName;
    private final LocalDate birthDate;
    private final String document;
    private final String email;
}
