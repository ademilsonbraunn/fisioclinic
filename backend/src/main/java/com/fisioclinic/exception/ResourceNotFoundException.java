package com.fisioclinic.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * ResourceNotFoundException — Recurso não encontrado (HTTP 404)
 * ─────────────────────────────────────────────────────────────────────────────
 * Lançada pelos Services quando um ID não existe no banco.
 * Capturada pelo GlobalExceptionHandler → resposta padronizada 404.
 * ─────────────────────────────────────────────────────────────────────────────
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
