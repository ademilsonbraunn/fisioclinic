package com.fisioclinic.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * LoginDTO — Entrada para autenticação via POST /api/auth/login
 * ─────────────────────────────────────────────────────────────────────────────
 * Camada: DTO (Data Transfer Object — request)
 * ─────────────────────────────────────────────────────────────────────────────
 */
public record LoginDTO(

    @NotBlank(message = "E-mail é obrigatório")
    @Email(message = "E-mail inválido")
    String email,

    @NotBlank(message = "Senha é obrigatória")
    String senha

) {}
