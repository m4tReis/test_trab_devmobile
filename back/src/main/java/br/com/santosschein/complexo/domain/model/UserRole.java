package br.com.santosschein.complexo.domain.model;

/**
 * Perfil do usuario (campo "perfil" da Parte 3 do relatorio).
 * O cadastro publico sempre cria CLIENTE; ADMIN e atribuido por uma operacao controlada no servidor.
 */
public enum UserRole {
    CLIENTE,
    ADMIN
}
