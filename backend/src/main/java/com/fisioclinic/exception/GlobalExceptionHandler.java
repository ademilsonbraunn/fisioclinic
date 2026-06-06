package com.fisioclinic.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * GlobalExceptionHandler — Tratamento centralizado de exceções da API
 * ─────────────────────────────────────────────────────────────────────────────
 * Camada: Exception / @RestControllerAdvice
 *
 * Garante que todas as respostas de erro seguem o padrão:
 *   { "erro": "mensagem", "status": <código HTTP> }
 *
 * Mapeamento de exceções:
 *  - ResourceNotFoundException → 404 Not Found
 *  - ConflictException         → 409 Conflict
 *  - MethodArgumentNotValidException → 400 Bad Request + mapa de campos inválidos
 *  - UnauthorizedException     → 401 Unauthorized
 *  - Exception (genérico)      → 500 Internal Server Error (loga o stack trace)
 *
 * O handler genérico loga "Erro interno não tratado" sem incluir dados do
 * request na mensagem — evita vazamento de informações sensíveis nos logs.
 * ─────────────────────────────────────────────────────────────────────────────
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
        return erro(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Map<String, Object>> handleConflict(ConflictException ex) {
        return erro(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> campos = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(e -> {
            String campo = e instanceof FieldError fe ? fe.getField() : e.getObjectName();
            campos.put(campo, e.getDefaultMessage());
        });

        Map<String, Object> body = new HashMap<>();
        body.put("erro", "Dados inválidos");
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("campos", campos);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Map<String, Object>> handleUnauthorized(UnauthorizedException ex) {
        return erro(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        log.error("Erro interno não tratado: {}", ex.getMessage(), ex);
        return erro(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno no servidor");
    }

    private ResponseEntity<Map<String, Object>> erro(HttpStatus status, String mensagem) {
        return ResponseEntity.status(status).body(Map.of(
            "erro", mensagem,
            "status", status.value()
        ));
    }
}
