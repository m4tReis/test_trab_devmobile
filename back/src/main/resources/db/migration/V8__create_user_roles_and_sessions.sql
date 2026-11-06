-- Parte 3 do relatorio: perfil do usuario (CLIENTE/ADMIN) e historico de acessos (acesso_usuario).

-- usuario.perfil: o cadastro publico sempre cria CLIENTE.
ALTER TABLE users ADD (role VARCHAR2(20) DEFAULT 'CLIENTE' NOT NULL);
ALTER TABLE users ADD CONSTRAINT ck_users_role CHECK (role IN ('CLIENTE', 'ADMIN'));

-- acesso_usuario: uma linha por sessao autenticada (cada login), com entrada e saida.
-- Instantes gravados em UTC; a interface converte para America/Sao_Paulo.
CREATE TABLE user_sessions (
    id VARCHAR2(36) PRIMARY KEY,
    user_id NUMBER NOT NULL,
    user_name VARCHAR2(201) NOT NULL,
    device VARCHAR2(120),
    login_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    logout_at TIMESTAMP,
    closed_at TIMESTAMP,
    close_reason VARCHAR2(20),
    CONSTRAINT fk_user_sessions_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT ck_user_sessions_reason CHECK (close_reason IN ('LOGOUT', 'EXPIRACAO')),
    CONSTRAINT ck_user_sessions_closed CHECK (
        (closed_at IS NULL AND close_reason IS NULL)
        OR (closed_at IS NOT NULL AND close_reason IS NOT NULL)
    ),
    CONSTRAINT ck_user_sessions_logout CHECK (
        (close_reason = 'LOGOUT' AND logout_at IS NOT NULL)
        OR ((close_reason IS NULL OR close_reason = 'EXPIRACAO') AND logout_at IS NULL)
    ),
    CONSTRAINT ck_user_sessions_validity CHECK (expires_at > login_at)
);

CREATE INDEX idx_user_sessions_login_at ON user_sessions (login_at);
CREATE INDEX idx_user_sessions_user_login ON user_sessions (user_id, login_at);
CREATE INDEX idx_user_sessions_open ON user_sessions (closed_at, expires_at);

COMMENT ON TABLE user_sessions IS 'acesso_usuario (Parte 3): uma linha por sessao autenticada, com login, validade e encerramento';
