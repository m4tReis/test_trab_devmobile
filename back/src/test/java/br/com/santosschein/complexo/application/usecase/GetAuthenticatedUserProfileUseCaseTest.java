package br.com.santosschein.complexo.application.usecase;

import br.com.santosschein.complexo.domain.model.User;
import br.com.santosschein.complexo.domain.repository.UserRepository;
import br.com.santosschein.complexo.interfaces.dto.response.UserProfileResponse;
import br.com.santosschein.complexo.shared.exception.InvalidCredentialsException;
import br.com.santosschein.complexo.testsupport.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetAuthenticatedUserProfileUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private GetAuthenticatedUserProfileUseCase useCase;

    @Test
    void returnsAuthenticatedUserProfile() {
        User user = TestDataFactory.user(123L);
        when(userRepository.findById(user.id())).thenReturn(Optional.of(user));

        UserProfileResponse response = useCase.execute(user.id());

        assertThat(response.id()).isEqualTo("123");
        assertThat(response.firstName()).isEqualTo("Joao");
        assertThat(response.lastName()).isEqualTo("Silva");
        assertThat(response.birthDate()).isEqualTo(LocalDate.of(1995, 4, 20));
        assertThat(response.document()).isEqualTo("12345678900");
        assertThat(response.email()).isEqualTo("joao@email.com");
    }

    @Test
    void profileResponseDoesNotExposePasswordOrHash() {
        assertThat(UserProfileResponse.class.getRecordComponents())
                .extracting(component -> component.getName())
                .doesNotContain("password", "passwordHash", "hash");
    }

    @Test
    void rejectsProfileWhenAuthenticatedUserDoesNotExist() {
        when(userRepository.findById(123L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(123L))
                .isInstanceOf(InvalidCredentialsException.class);
    }

}
