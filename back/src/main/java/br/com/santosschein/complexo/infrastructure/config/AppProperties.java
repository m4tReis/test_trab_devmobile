package br.com.santosschein.complexo.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        Jwt jwt,
        Reservation reservation
) {

    public record Jwt(
            String secret,
            long expirationSeconds
    ) {
    }

    public record Reservation(
            int slotMinutes,
            int minimumCancelHours
    ) {
    }
}
