package br.com.santosschein.complexo.application.usecase.admin;

import br.com.santosschein.complexo.domain.repository.UserRepository;
import br.com.santosschein.complexo.interfaces.dto.response.AdminUserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Lista de usuarios para o filtro "Usuario" da area administrativa (sem dados sensiveis).
 */
@Service
@RequiredArgsConstructor
public class ListUsersForAdminUseCase {

    private final UserRepository userRepository;

    public List<AdminUserResponse> execute() {
        return userRepository.findAllOrderedByName()
                .stream()
                .map(user -> new AdminUserResponse(user.id(), user.fullName(), user.email(), user.role().name()))
                .toList();
    }
}
