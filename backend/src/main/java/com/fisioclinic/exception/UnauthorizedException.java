package com.fisioclinic.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * UnauthorizedException — Operação não autorizada (HTTP 401)
 * ─────────────────────────────────────────────────────────────────────────────
 * Lançada pelo AuthService quando credenciais são inválidas ou quando a
 * senha atual informada não confere na troca de senha.
 * Capturada pelo GlobalExceptionHandler → resposta padronizada 401.
 * ─────────────────────────────────────────────────────────────────────────────
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
