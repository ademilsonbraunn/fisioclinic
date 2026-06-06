package com.fisioclinic.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * ConflictException — Conflito de dados (HTTP 409)
 * ─────────────────────────────────────────────────────────────────────────────
 * Lançada quando uma operação viola uma regra de unicidade ou exclusividade:
 *  - CPF duplicado no cadastro de pacientes
 *  - E-mail ou CRF duplicado no cadastro de fisioterapeutas
 *  - Conflito de horário na mesma sala (regra de agendamento)
 *  - Tentativa de registrar segunda evolução na mesma sessão
 * ─────────────────────────────────────────────────────────────────────────────
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
