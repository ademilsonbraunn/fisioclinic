package com.fisioclinic.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * TermoConsentimentoDTO — Entrada para registro de TCLE (Módulo 3)
 * ─────────────────────────────────────────────────────────────────────────────
 * Camada: DTO (Data Transfer Object — request)
 *
 * assinado_em é obrigatório — exigência legal para registro com validade.
 * O frontend envia new Date().toISOString() no momento do clique em "Salvar".
 * ─────────────────────────────────────────────────────────────────────────────
 */
public record TermoConsentimentoDTO(

    @JsonProperty("paciente_id")
    @NotNull(message = "Paciente é obrigatório")
    UUID pacienteId,

    @JsonProperty("plano_id")
    UUID planoId,

    String tipo,

    String conteudo,

    @JsonProperty("assinado_em")
    @NotNull(message = "Data/hora da assinatura é obrigatória")
    LocalDateTime assinadoEm

) {}
