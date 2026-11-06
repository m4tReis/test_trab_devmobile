package br.com.santosschein.complexo.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record User(
        Long id,
        String firstName,
        String lastName,
        LocalDate birthDate,
        String document,
        String email,
        String passwordHash,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        UserRole role
) {

    public User {
        if (role == null) {
            role = UserRole.CLIENTE;
        }
    }

    /**
     * Construtor de conveniencia: usuario com o perfil padrao CLIENTE.
     */
    public User(
            Long id,
            String firstName,
            String lastName,
            LocalDate birthDate,
            String document,
            String email,
            String passwordHash,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this(id, firstName, lastName, birthDate, document, email, passwordHash, createdAt, updatedAt, UserRole.CLIENTE);
    }

    public String fullName() {
        return ((firstName == null ? "" : firstName) + " " + (lastName == null ? "" : lastName)).trim();
    }
}
