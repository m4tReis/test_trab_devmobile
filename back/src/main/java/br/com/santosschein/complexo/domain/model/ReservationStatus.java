package br.com.santosschein.complexo.domain.model;

public enum ReservationStatus {
    ACTIVE,
    CANCELED,
    /**
     * Situacao derivada: reserva ativa cujo horario ja terminou.
     * Nao e gravada no banco; e calculada ao montar a resposta da API.
     */
    COMPLETED
}
