package com.santosschein.app.data.remote.mapper;

import com.santosschein.app.data.remote.dto.UserProfileDto;
import com.santosschein.app.domain.model.User;

import java.time.LocalDate;

public final class UserMapper {

    public User toDomain(UserProfileDto dto) {
        return new User(
                dto.getId(),
                dto.getFirstName(),
                dto.getLastName(),
                LocalDate.parse(dto.getBirthDate()),
                dto.getDocument(),
                dto.getEmail()
        );
    }
}
