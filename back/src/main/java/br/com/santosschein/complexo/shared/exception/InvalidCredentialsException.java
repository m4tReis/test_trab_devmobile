package br.com.santosschein.complexo.shared.exception;

public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Credenciais invalidas.");
    }
}
