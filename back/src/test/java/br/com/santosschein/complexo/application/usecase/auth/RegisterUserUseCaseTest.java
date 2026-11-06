package br.com.santosschein.complexo.application.usecase.auth;

import br.com.santosschein.complexo.application.service.PasswordService;
import br.com.santosschein.complexo.domain.model.User;
import br.com.santosschein.complexo.domain.model.UserRole;
import br.com.santosschein.complexo.domain.repository.UserRepository;
import br.com.santosschein.complexo.interfaces.dto.request.RegisterUserRequest;
import br.com.santosschein.complexo.shared.exception.BusinessException;
import br.com.santosschein.complexo.shared.exception.UserAlreadyExistsException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordService passwordService;

    @InjectMocks
    private RegisterUserUseCase useCase;

    @Test
    void registersUserSuccessfully() {
        RegisterUserRequest request = validRequest();
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userRepository.existsByDocument(request.document())).thenReturn(false);
        when(passwordService.encode(request.password())).thenReturn("$2a$hash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        useCase.execute(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.firstName()).isEqualTo("Joao");
        assertThat(savedUser.lastName()).isEqualTo("Silva");
        assertThat(savedUser.email()).isEqualTo("joao@email.com");
        assertThat(savedUser.document()).isEqualTo("12345678900");
        assertThat(savedUser.passwordHash()).isEqualTo("$2a$hash");
        assertThat(savedUser.passwordHash()).isNotEqualTo(request.password());
        assertThat(savedUser.createdAt()).isNotNull();
        assertThat(savedUser.updatedAt()).isNotNull();
    }

    @Test
    void rejectsRegistrationWhenEmailAlreadyExists() {
        RegisterUserRequest request = validRequest();
        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(UserAlreadyExistsException.class);

        verify(userRepository, never()).existsByDocument(request.document());
        verify(passwordService, never()).encode(request.password());
        verify(userRepository, never()).save(any());
    }

    @Test
    void rejectsRegistrationWhenDocumentAlreadyExists() {
        RegisterUserRequest request = validRequest();
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userRepository.existsByDocument(request.document())).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(UserAlreadyExistsException.class);

        verify(passwordService, never()).encode(request.password());
        verify(userRepository, never()).save(any());
    }

    @Test
    void normalizesEmailAndCpfAndAlwaysCreatesClient() {
        RegisterUserRequest request = new RegisterUserRequest(
                "  Joao ",
                "Silva",
                LocalDate.of(1995, 4, 20),
                "123.456.789-00",
                "  Joao@Email.COM ",
                "Senha@123"
        );
        when(userRepository.existsByEmail("joao@email.com")).thenReturn(false);
        when(userRepository.existsByDocument("12345678900")).thenReturn(false);
        when(passwordService.encode(request.password())).thenReturn("$2a$hash");

        useCase.execute(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().email()).isEqualTo("joao@email.com");
        assertThat(userCaptor.getValue().document()).isEqualTo("12345678900");
        assertThat(userCaptor.getValue().firstName()).isEqualTo("Joao");
        assertThat(userCaptor.getValue().role()).isEqualTo(UserRole.CLIENTE);
    }

    @Test
    void rejectsCpfWithoutElevenDigits() {
        RegisterUserRequest request = new RegisterUserRequest(
                "Joao",
                "Silva",
                LocalDate.of(1995, 4, 20),
                "123.456.789",
                "joao@email.com",
                "Senha@123"
        );

        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(BusinessException.class);

        verify(userRepository, never()).save(any());
    }

    private RegisterUserRequest validRequest() {
        return new RegisterUserRequest(
                "Joao",
                "Silva",
                LocalDate.of(1995, 4, 20),
                "12345678900",
                "joao@email.com",
                "Senha@123"
        );
    }
}
