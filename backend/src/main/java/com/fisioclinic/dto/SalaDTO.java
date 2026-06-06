package com.fisioclinic.dto;

import com.fisioclinic.model.Sala;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * SalaDTO — Entrada para criação e atualização de sala/box
 * ─────────────────────────────────────────────────────────────────────────────
 * Camada: DTO (Data Transfer Object — request)
 * ─────────────────────────────────────────────────────────────────────────────
 */
public record SalaDTO(

    @NotBlank(message = "Nome é obrigatório")
    String nome,

    @NotNull(message = "Tipo é obrigatório")
    Sala.TipoSala tipo,

    @Min(value = 1, message = "Capacidade mínima é 1")
    Integer capacidade,

    Boolean ativo

) {}
