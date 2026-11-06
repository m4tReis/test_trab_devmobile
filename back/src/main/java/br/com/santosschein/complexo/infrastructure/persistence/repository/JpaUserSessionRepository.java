package br.com.santosschein.complexo.infrastructure.persistence.repository;

import br.com.santosschein.complexo.domain.model.SessionCloseReason;
import br.com.santosschein.complexo.infrastructure.persistence.entity.UserSessionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface JpaUserSessionRepository extends JpaRepository<UserSessionEntity, String> {

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update UserSessionEntity s
               set s.logoutAt = :now,
                   s.closedAt = :now,
                   s.closeReason = :reason
             where s.id = :id
               and s.closedAt is null
               and s.expiresAt > :now
            """)
    int closeByLogout(
            @Param("id") String id,
            @Param("now") LocalDateTime now,
            @Param("reason") SessionCloseReason reason
    );

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update UserSessionEntity s
               set s.closedAt = s.expiresAt,
                   s.closeReason = :reason
             where s.closedAt is null
               and s.expiresAt <= :now
            """)
    int closeExpired(@Param("now") LocalDateTime now, @Param("reason") SessionCloseReason reason);

    Page<UserSessionEntity> findByLoginAtGreaterThanEqualAndLoginAtLessThanOrderByLoginAtAscIdAsc(
            LocalDateTime from,
            LocalDateTime to,
            Pageable pageable
    );

    Page<UserSessionEntity> findByUserIdAndLoginAtGreaterThanEqualAndLoginAtLessThanOrderByLoginAtAscIdAsc(
            Long userId,
            LocalDateTime from,
            LocalDateTime to,
            Pageable pageable
    );
}
