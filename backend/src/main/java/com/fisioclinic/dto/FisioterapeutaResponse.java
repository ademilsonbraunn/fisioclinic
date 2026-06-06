package com.fisioclinic.dto;

import com.fisioclinic.model.Fisioterapeuta;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * FisioterapeutaResponse — Saída da API para leitura de fisioterapeuta
 * ─────────────────────────────────────────────────────────────────────────────
 * Camada: DTO (Data Transfer Object — response)
 *
 * Nunca expõe senhaHash — o campo é omitido intencionalmente neste record.
 * ─────────────────────────────────────────────────────────────────────────────
 */
public record FisioterapeutaResponse(

    UUID id,
    String nome,
    String crf,
    String email,
    String telefone,
    Boolean ativo,
    Fisioterapeuta.Perfil perfil,
    LocalDateTime createdAt,
    LocalDateTime updatedAt

) {}
