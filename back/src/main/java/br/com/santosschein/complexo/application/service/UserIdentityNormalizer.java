package br.com.santosschein.complexo.application.service;

import java.util.Locale;

/**
 * Normaliza os dados de identificacao antes de gravar ou consultar (RF01):
 * e-mail sem espacos e em minusculas; CPF somente com digitos.
 */
public final class UserIdentityNormalizer {

    private UserIdentityNormalizer() {
    }

    public static String email(String value) {
        return value == null ? null : value.trim().toLowerCase(Locale.ROOT);
    }

    public static String document(String value) {
        return value == null ? null : value.replaceAll("\\D", "");
    }

    public static String name(String value) {
        return value == null ? null : value.trim().replaceAll("\\s+", " ");
    }
}
