package com.fisioclinic.dto;

import com.fisioclinic.model.Sala;

import java.util.UUID;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * SalaResponse — Saída da API para leitura de sala/box
 * ─────────────────────────────────────────────────────────────────────────────
 * Camada: DTO (Data Transfer Object — response)
 * ─────────────────────────────────────────────────────────────────────────────
 */
public record SalaResponse(

    UUID id,
    String nome,
    Sala.TipoSala tipo,
    Integer capacidade,
    Boolean ativo

) {}
