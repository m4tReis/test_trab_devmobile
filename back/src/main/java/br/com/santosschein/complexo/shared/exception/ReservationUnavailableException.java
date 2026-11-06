package br.com.santosschein.complexo.shared.exception;

public class ReservationUnavailableException extends RuntimeException {

    public ReservationUnavailableException(String message) {
        super(message);
    }
}
