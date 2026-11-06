package br.com.santosschein.complexo.infrastructure.persistence.entity;

import br.com.santosschein.complexo.domain.model.SessionCloseReason;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Tabela user_sessions (acesso_usuario no relatorio). Instantes em UTC.
 */
@Entity
@Table(name = "user_sessions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class UserSessionEntity {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "user_name", nullable = false, length = 201)
    private String userName;

    @Column(length = 120)
    private String device;

    @Column(name = "login_at", nullable = false)
    private LocalDateTime loginAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "logout_at")
    private LocalDateTime logoutAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "close_reason", length = 20)
    private SessionCloseReason closeReason;
}
