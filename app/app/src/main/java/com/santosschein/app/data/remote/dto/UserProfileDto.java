package com.santosschein.app.data.remote.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public final class UserProfileDto {

    private String id;
    private String firstName;
    private String lastName;
    private String birthDate;
    private String document;
    private String email;
}
