package br.com.santosschein.complexo.application.usecase.auth;

import br.com.santosschein.complexo.application.service.PasswordService;
import br.com.santosschein.complexo.application.service.UserIdentityNormalizer;
import br.com.santosschein.complexo.domain.model.User;
import br.com.santosschein.complexo.domain.model.UserRole;
import br.com.santosschein.complexo.domain.repository.UserRepository;
import br.com.santosschein.complexo.interfaces.dto.request.RegisterUserRequest;
import br.com.santosschein.complexo.shared.exception.BusinessException;
import br.com.santosschein.complexo.shared.exception.UserAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RegisterUserUseCase {

    private static final int CPF_LENGTH = 11;

    private final UserRepository userRepository;
    private final PasswordService passwordService;

    public void execute(RegisterUserRequest request) {
        String email = UserIdentityNormalizer.email(request.email());
        String document = UserIdentityNormalizer.document(request.document());

        if (document == null || document.length() != CPF_LENGTH) {
            throw new BusinessException("CPF invalido. Informe os 11 digitos.");
        }

        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException("E-mail ja cadastrado.");
        }

        if (userRepository.existsByDocument(document)) {
            throw new UserAlreadyExistsException("Documento ja cadastrado.");
        }

        LocalDateTime now = LocalDateTime.now();
        User user = new User(
                null,
                UserIdentityNormalizer.name(request.firstName()),
                UserIdentityNormalizer.name(request.lastName()),
                request.birthDate(),
                document,
                email,
                passwordService.encode(request.password()),
                now,
                now,
                // O cadastro publico sempre cria um cliente; ADMIN so por operacao controlada no servidor.
                UserRole.CLIENTE
        );

        userRepository.save(user);
    }
}
