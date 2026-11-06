package br.com.santosschein.complexo.infrastructure.persistence.mapper;

import br.com.santosschein.complexo.domain.model.User;
import br.com.santosschein.complexo.infrastructure.persistence.entity.UserEntity;

public class UserPersistenceMapper {

    public User toDomain(UserEntity entity) {
        return new User(
                entity.getId(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getBirthDate(),
                entity.getDocument(),
                entity.getEmail(),
                entity.getPasswordHash(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getRole()
        );
    }

    public UserEntity toEntity(User user) {
        return new UserEntity(
                user.id(),
                user.firstName(),
                user.lastName(),
                user.birthDate(),
                user.document(),
                user.email(),
                user.passwordHash(),
                user.createdAt(),
                user.updatedAt(),
                user.role()
        );
    }
}
