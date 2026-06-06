package com.fisioclinic.dto;

import com.fisioclinic.model.Fisioterapeuta;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * FisioterapeutaDTO — Entrada para criação e atualização de fisioterapeuta
 * ─────────────────────────────────────────────────────────────────────────────
 * Camada: DTO (Data Transfer Object — request)
 *
 * POST:  usar @Valid — campos marcados com @Not* são validados.
 * PATCH: não usar @Valid — campos nulos significam "manter valor atual".
 *
 * Campo senha: plaintext recebido aqui, nunca persisto — o FisioterapeutaService
 * aplica BCrypt antes de gravar senhaHash. Se null no PATCH, a senha não é alterada.
 * ─────────────────────────────────────────────────────────────────────────────
 */
public record FisioterapeutaDTO(

    @NotBlank(message = "Nome é obrigatório")
    String nome,

    @NotBlank(message = "CRF é obrigatório")
    @Size(max = 15, message = "CRF deve ter no máximo 15 caracteres")
    String crf,

    @NotBlank(message = "E-mail é obrigatório")
    @Email(message = "E-mail inválido")
    String email,

    String telefone,

    String senha,

    Fisioterapeuta.Perfil perfil,

    Boolean ativo

) {}
