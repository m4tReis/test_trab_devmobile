package br.com.santosschein.complexo.infrastructure.persistence.repository;

import br.com.santosschein.complexo.domain.model.PageResult;
import br.com.santosschein.complexo.domain.model.SessionCloseReason;
import br.com.santosschein.complexo.domain.model.UserSession;
import br.com.santosschein.complexo.domain.repository.UserSessionRepository;
import br.com.santosschein.complexo.infrastructure.persistence.entity.UserSessionEntity;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserSessionRepositoryAdapter implements UserSessionRepository {

    private final JpaUserSessionRepository jpaUserSessionRepository;
    private final EntityManager entityManager;

    @Override
    public UserSession create(UserSession session) {
        UserSessionEntity entity = toEntity(session);
        // persist + flush: o INSERT acontece aqui, antes de o token ser emitido.
        entityManager.persist(entity);
        entityManager.flush();
        return toDomain(entity);
    }

    @Override
    public Optional<UserSession> findById(String id) {
        return jpaUserSessionRepository.findById(id).map(this::toDomain);
    }

    @Override
    public boolean closeByLogout(String id, LocalDateTime nowUtc) {
        return jpaUserSessionRepository.closeByLogout(id, nowUtc, SessionCloseReason.LOGOUT) == 1;
    }

    @Override
    public int closeExpired(LocalDateTime nowUtc) {
        return jpaUserSessionRepository.closeExpired(nowUtc, SessionCloseReason.EXPIRACAO);
    }

    @Override
    public PageResult<UserSession> search(
            Long userId,
            LocalDateTime fromUtc,
            LocalDateTime toUtcExclusive,
            int page,
            int size
    ) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<UserSessionEntity> result = userId == null
                ? jpaUserSessionRepository.findByLoginAtGreaterThanEqualAndLoginAtLessThanOrderByLoginAtAscIdAsc(
                        fromUtc, toUtcExclusive, pageRequest)
                : jpaUserSessionRepository.findByUserIdAndLoginAtGreaterThanEqualAndLoginAtLessThanOrderByLoginAtAscIdAsc(
                        userId, fromUtc, toUtcExclusive, pageRequest);

        return new PageResult<>(
                result.getContent().stream().map(this::toDomain).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    private UserSession toDomain(UserSessionEntity entity) {
        return new UserSession(
                entity.getId(),
                entity.getUserId(),
                entity.getUserName(),
                entity.getDevice(),
                entity.getLoginAt(),
                entity.getExpiresAt(),
                entity.getLogoutAt(),
                entity.getClosedAt(),
                entity.getCloseReason()
        );
    }

    private UserSessionEntity toEntity(UserSession session) {
        return new UserSessionEntity(
                session.id(),
                session.userId(),
                session.userName(),
                session.device(),
                session.loginAt(),
                session.expiresAt(),
                session.logoutAt(),
                session.closedAt(),
                session.closeReason()
        );
    }
}
