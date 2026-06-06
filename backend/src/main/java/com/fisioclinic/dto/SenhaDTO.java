package com.fisioclinic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * SenhaDTO — Entrada para alteração de senha via POST /api/auth/alterar-senha
 * ─────────────────────────────────────────────────────────────────────────────
 * Camada: DTO (Data Transfer Object — request)
 *
 * senhaAtual é verificada no AuthService contra o hash armazenado (BCrypt)
 * antes de aceitar a nova senha — impede troca de senha sem conhecer a atual.
 * ─────────────────────────────────────────────────────────────────────────────
 */
public record SenhaDTO(

    @NotBlank(message = "Senha atual é obrigatória")
    String senhaAtual,

    @NotBlank(message = "Nova senha é obrigatória")
    @Size(min = 8, message = "A nova senha deve ter no mínimo 8 caracteres")
    String novaSenha

) {}
