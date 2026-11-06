package br.com.santosschein.complexo.application.usecase.admin;

import br.com.santosschein.complexo.domain.model.PageResult;
import br.com.santosschein.complexo.domain.model.UserSession;
import br.com.santosschein.complexo.domain.repository.UserSessionRepository;
import br.com.santosschein.complexo.interfaces.dto.response.AccessLogPageResponse;
import br.com.santosschein.complexo.interfaces.dto.response.AccessLogResponse;
import br.com.santosschein.complexo.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * RF12: consulta somente leitura do historico de acessos, com filtro por periodo e usuario e paginacao.
 * Antes da consulta, as sessoes vencidas sao encerradas como EXPIRACAO para a situacao ficar correta.
 */
@Service
@RequiredArgsConstructor
public class ListAccessLogsUseCase {

    public static final ZoneId DISPLAY_ZONE = ZoneId.of("America/Sao_Paulo");

    static final int DEFAULT_PAGE_SIZE = 20;
    static final int MAX_PAGE_SIZE = 100;

    private static final LocalDateTime OLDEST = LocalDateTime.of(1970, 1, 1, 0, 0);
    private static final LocalDateTime NEWEST = LocalDateTime.of(9999, 12, 31, 0, 0);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final UserSessionRepository userSessionRepository;
    private final Clock clock;

    @Transactional
    public AccessLogPageResponse execute(LocalDate from, LocalDate to, Long userId, Integer page, Integer size) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new BusinessException("Periodo invalido: a data inicial e posterior a data final.");
        }

        int pageNumber = page == null || page < 0 ? 0 : page;
        int pageSize = size == null || size < 1 ? DEFAULT_PAGE_SIZE : Math.min(size, MAX_PAGE_SIZE);

        LocalDateTime nowUtc = LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
        userSessionRepository.closeExpired(nowUtc);

        // O periodo e informado em datas locais (America/Sao_Paulo) e convertido para UTC; "ate" e inclusivo.
        LocalDateTime fromUtc = from == null ? OLDEST : startOfDayUtc(from);
        LocalDateTime toUtcExclusive = to == null ? NEWEST : startOfDayUtc(to.plusDays(1));

        PageResult<UserSession> result = userSessionRepository.search(
                userId,
                fromUtc,
                toUtcExclusive,
                pageNumber,
                pageSize
        );

        return new AccessLogPageResponse(
                result.items().stream().map(session -> toResponse(session, nowUtc)).toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages()
        );
    }

    private AccessLogResponse toResponse(UserSession session, LocalDateTime nowUtc) {
        return new AccessLogResponse(
                session.userId(),
                session.userName(),
                session.device(),
                isoUtc(session.loginAt()),
                localDate(session.loginAt()),
                localTime(session.loginAt()),
                isoUtc(session.logoutAt()),
                localDate(session.logoutAt()),
                localTime(session.logoutAt()),
                isoUtc(session.expiresAt()),
                isoUtc(session.closedAt()),
                session.statusAt(nowUtc).name()
        );
    }

    private static LocalDateTime startOfDayUtc(LocalDate date) {
        return date.atStartOfDay(DISPLAY_ZONE).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
    }

    private static String isoUtc(LocalDateTime utc) {
        return utc == null ? null : utc.toInstant(ZoneOffset.UTC).toString();
    }

    private static String localDate(LocalDateTime utc) {
        return utc == null ? null : utc.atOffset(ZoneOffset.UTC).atZoneSameInstant(DISPLAY_ZONE).format(DATE_FORMAT);
    }

    private static String localTime(LocalDateTime utc) {
        return utc == null ? null : utc.atOffset(ZoneOffset.UTC).atZoneSameInstant(DISPLAY_ZONE).format(TIME_FORMAT);
    }
}
