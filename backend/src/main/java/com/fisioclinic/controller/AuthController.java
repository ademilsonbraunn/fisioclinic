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
