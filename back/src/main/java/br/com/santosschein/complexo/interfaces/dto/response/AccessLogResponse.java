package br.com.santosschein.complexo.interfaces.dto.response;

/**
 * Uma linha do historico de acessos (RF11/RF12). Nao expoe senha, hash nem token.
 * <p>
 * Os campos *Date/*Time ja vem no fuso America/Sao_Paulo (dd/MM/yyyy e HH:mm:ss);
 * os campos *At trazem o mesmo instante em UTC (ISO 8601).
 *
 * @param status EM_ABERTO, LOGOUT (saida confirmada) ou EXPIRADA.
 */
public record AccessLogResponse(
        Long userId,
        String userName,
        String device,
        String loginAt,
        String loginDate,
        String loginTime,
        String logoutAt,
        String logoutDate,
        String logoutTime,
        String expiresAt,
        String closedAt,
        String status
) {
}
