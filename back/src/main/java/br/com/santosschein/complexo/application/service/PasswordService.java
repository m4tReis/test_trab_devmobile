package br.com.santosschein.complexo.application.service;

public interface PasswordService {

    String encode(String rawPassword);

    boolean matches(String rawPassword, String encodedPassword);
}
