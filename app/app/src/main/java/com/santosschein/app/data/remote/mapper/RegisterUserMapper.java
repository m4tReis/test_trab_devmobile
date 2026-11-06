package com.santosschein.app.data.remote.mapper;

import com.santosschein.app.data.remote.dto.RegisterUserRequestDto;
import com.santosschein.app.domain.model.RegisterUserInput;

public final class RegisterUserMapper {

    public RegisterUserRequestDto toDto(RegisterUserInput input) {
        return new RegisterUserRequestDto(
                input.getFirstName(),
                input.getLastName(),
                input.getBirthDate().toString(),
                input.getDocument(),
                input.getEmail(),
                input.getPassword()
        );
    }
}
