package com.fisioclinic.controller;

import com.fisioclinic.dto.LoginDTO;
import com.fisioclinic.dto.SenhaDTO;
import com.fisioclinic.dto.TokenResponse;
import com.fisioclinic.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * AuthController — Autenticação e gerenciamento de senha
 * ─────────────────────────────────────────────────────────────────────────────
 * Camada: Controller (entrada HTTP)
 * Base URL: /api/auth
 * Acesso: /login é público; /senha exige autenticação prévia
 *
 * Endpoints:
 *  POST  /api/auth/login  → valida credenciais e devolve token JWT
 *  PATCH /api/auth/senha  → altera a senha do fisioterapeuta autenticado
 *
 * O objeto Authentication (injetado pelo Spring Security no PATCH /senha)
 * contém o e-mail extraído do token JWT pelo JwtFilter — não vem do request body.
 * ─────────────────────────────────────────────────────────────────────────────
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * POST /api/auth/login
     * Autentica o usuário e retorna o token JWT.
     */
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginDTO dto) {
        return ResponseEntity.ok(authService.login(dto));
    }

    /**
     * PATCH /api/auth/senha
     * Altera a senha do usuário autenticado.
     */
    @PatchMapping("/senha")
    public ResponseEntity<Void> alterarSenha(
        Authentication authentication,
        @Valid @RequestBody SenhaDTO dto
    ) {
        authService.alterarSenha(authentication.getName(), dto);
        return ResponseEntity.noContent().build();
    }
}
