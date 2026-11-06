package br.com.santosschein.complexo.security;

import br.com.santosschein.complexo.domain.model.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class AuthenticatedUser implements UserDetails {

    private final Long id;
    private final String email;
    private final String passwordHash;
    private final UserRole role;
    private final String sessionId;

    public AuthenticatedUser(Long id, String email, String passwordHash) {
        this(id, email, passwordHash, UserRole.CLIENTE, null);
    }

    public AuthenticatedUser(Long id, String email, String passwordHash, UserRole role, String sessionId) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role == null ? UserRole.CLIENTE : role;
        this.sessionId = sessionId;
    }

    public Long id() {
        return id;
    }

    public UserRole role() {
        return role;
    }

    /**
     * Sessao (acesso) a que esta requisicao pertence.
     */
    public String sessionId() {
        return sessionId;
    }

    public AuthenticatedUser withSessionId(String newSessionId) {
        return new AuthenticatedUser(id, email, passwordHash, role, newSessionId);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }
}
