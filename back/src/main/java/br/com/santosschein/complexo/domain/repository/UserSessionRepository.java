package br.com.santosschein.complexo.domain.repository;

import br.com.santosschein.complexo.domain.model.PageResult;
import br.com.santosschein.complexo.domain.model.UserSession;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UserSessionRepository {

    /**
     * Grava um novo acesso. Deve falhar (excecao) se o registro nao puder ser gravado.
     */
    UserSession create(UserSession session);

    Optional<UserSession> findById(String id);

    /**
     * Encerra por logout, em uma unica operacao atomica, somente a sessao informada e somente se
     * ela ainda estiver aberta e valida. Preenche logout_at e closed_at com o mesmo instante.
     *
     * @return true se a sessao foi encerrada agora; false se ja estava encerrada ou vencida.
     */
    boolean closeByLogout(String id, LocalDateTime nowUtc);

    /**
     * Encerra como EXPIRACAO todas as sessoes abertas cuja validade terminou (closed_at = expires_at).
     *
     * @return quantidade de sessoes encerradas.
     */
    int closeExpired(LocalDateTime nowUtc);

    /**
     * Historico ordenado pelo login. Periodo [fromUtc, toUtcExclusive); userId opcional.
     */
    PageResult<UserSession> search(Long userId, LocalDateTime fromUtc, LocalDateTime toUtcExclusive, int page, int size);
}
