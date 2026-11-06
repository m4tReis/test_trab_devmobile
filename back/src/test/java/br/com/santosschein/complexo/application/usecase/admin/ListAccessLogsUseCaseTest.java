package br.com.santosschein.complexo.application.usecase.admin;

import br.com.santosschein.complexo.domain.model.PageResult;
import br.com.santosschein.complexo.domain.model.SessionCloseReason;
import br.com.santosschein.complexo.domain.model.UserSession;
import br.com.santosschein.complexo.domain.repository.UserSessionRepository;
import br.com.santosschein.complexo.interfaces.dto.response.AccessLogPageResponse;
import br.com.santosschein.complexo.interfaces.dto.response.AccessLogResponse;
import br.com.santosschein.complexo.shared.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListAccessLogsUseCaseTest {

    /** 23/10/2026 19:40 em Sao Paulo. */
    private static final Instant NOW = Instant.parse("2026-10-23T22:40:00Z");
    private static final LocalDateTime NOW_UTC = LocalDateTime.of(2026, 10, 23, 22, 40);

    @Mock
    private UserSessionRepository userSessionRepository;

    private ListAccessLogsUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ListAccessLogsUseCase(userSessionRepository, Clock.fixed(NOW, ZoneId.of("UTC")));
    }

    @Test
    void showsExampleOfTheReportWithLocalDateAndTime() {
        // Exemplo da Parte 3: A1 (Android A) 19:00 -> 19:30 LOGOUT; A2 (Android B) 19:10 em aberto.
        UserSession a1 = new UserSession(
                "A1", 7L, "Ana Silva", "Android A",
                LocalDateTime.of(2026, 10, 23, 22, 0),
                LocalDateTime.of(2026, 10, 23, 23, 0),
                LocalDateTime.of(2026, 10, 23, 22, 30),
                LocalDateTime.of(2026, 10, 23, 22, 30),
                SessionCloseReason.LOGOUT
        );
        UserSession a2 = new UserSession(
                "A2", 7L, "Ana Silva", "Android B",
                LocalDateTime.of(2026, 10, 23, 22, 10),
                LocalDateTime.of(2026, 10, 23, 23, 10),
                null, null, null
        );
        when(userSessionRepository.search(isNull(), any(), any(), eq(0), eq(20)))
                .thenReturn(new PageResult<>(List.of(a1, a2), 0, 20, 2, 1));

        AccessLogPageResponse response = useCase.execute(null, null, null, null, null);

        assertThat(response.totalElements()).isEqualTo(2);
        AccessLogResponse first = response.items().get(0);
        assertThat(first.userName()).isEqualTo("Ana Silva");
        assertThat(first.loginDate()).isEqualTo("23/10/2026");
        assertThat(first.loginTime()).isEqualTo("19:00:00");
        assertThat(first.logoutDate()).isEqualTo("23/10/2026");
        assertThat(first.logoutTime()).isEqualTo("19:30:00");
        assertThat(first.loginAt()).isEqualTo("2026-10-23T22:00:00Z");
        assertThat(first.status()).isEqualTo("LOGOUT");

        AccessLogResponse second = response.items().get(1);
        assertThat(second.loginTime()).isEqualTo("19:10:00");
        assertThat(second.logoutDate()).isNull();
        assertThat(second.logoutTime()).isNull();
        assertThat(second.status()).isEqualTo("EM_ABERTO");
    }

    @Test
    void closesExpiredSessionsBeforeSearching() {
        when(userSessionRepository.search(any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(new PageResult<>(List.of(), 0, 20, 0, 0));

        useCase.execute(null, null, null, 0, 20);

        InOrder order = inOrder(userSessionRepository);
        order.verify(userSessionRepository).closeExpired(NOW_UTC);
        order.verify(userSessionRepository).search(any(), any(), any(), anyInt(), anyInt());
    }

    @Test
    void convertsLocalPeriodToUtcAndIncludesTheWholeLastDay() {
        when(userSessionRepository.search(any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(new PageResult<>(List.of(), 0, 20, 0, 0));

        useCase.execute(LocalDate.of(2026, 10, 23), LocalDate.of(2026, 10, 24), 7L, 0, 20);

        // 23/10 00:00 em Sao Paulo (UTC-3) = 23/10 03:00 UTC; fim exclusivo: 25/10 00:00 local = 25/10 03:00 UTC.
        verify(userSessionRepository).search(
                7L,
                LocalDateTime.of(2026, 10, 23, 3, 0),
                LocalDateTime.of(2026, 10, 25, 3, 0),
                0,
                20
        );
    }

    @Test
    void limitsPageSize() {
        when(userSessionRepository.search(any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(new PageResult<>(List.of(), 0, 100, 0, 0));

        useCase.execute(null, null, null, -1, 5000);

        verify(userSessionRepository).search(isNull(), any(), any(), eq(0), eq(100));
    }

    @Test
    void rejectsInvertedPeriod() {
        assertThatThrownBy(() -> useCase.execute(LocalDate.of(2026, 10, 24), LocalDate.of(2026, 10, 23), null, 0, 20))
                .isInstanceOf(BusinessException.class);

        verifyNoInteractions(userSessionRepository);
    }

    @Test
    void sessionPastValidityIsShownAsExpiredWithoutLogout() {
        UserSession expired = new UserSession(
                "A3", 7L, "Ana Silva", null,
                LocalDateTime.of(2026, 10, 23, 20, 0),
                LocalDateTime.of(2026, 10, 23, 21, 0),
                null,
                LocalDateTime.of(2026, 10, 23, 21, 0),
                SessionCloseReason.EXPIRACAO
        );
        when(userSessionRepository.search(any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(new PageResult<>(List.of(expired), 0, 20, 1, 1));

        AccessLogResponse item = useCase.execute(null, null, null, 0, 20).items().getFirst();

        assertThat(item.status()).isEqualTo("EXPIRADA");
        assertThat(item.logoutTime()).isNull();
        assertThat(item.closedAt()).isEqualTo("2026-10-23T21:00:00Z");
    }
}
