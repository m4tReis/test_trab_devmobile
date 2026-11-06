package br.com.santosschein.complexo.interfaces.dto.response;

public record AdminUserResponse(
        Long id,
        String name,
        String email,
        String role
) {
}
