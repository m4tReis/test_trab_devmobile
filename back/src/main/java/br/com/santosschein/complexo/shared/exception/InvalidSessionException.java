package br.com.santosschein.complexo.shared.exception;

public class InvalidSessionException extends RuntimeException {

    public InvalidSessionException() {
        super("Sessao expirada. Entre novamente.");
    }
}
