package com.fisioclinic.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * TermoConsentimentoResponse — Saída da API para leitura de TCLE (Módulo 3)
 * ─────────────────────────────────────────────────────────────────────────────
 */
public record TermoConsentimentoResponse(

    UUID id,

    String tipo,

    @JsonProperty("assinado_em")
    LocalDateTime assinadoEm,

    @JsonProperty("created_at")
    LocalDateTime createdAt,

    @JsonProperty("plano_resumo")
    PlanoResumo planoResumo

) {
    public record PlanoResumo(
        UUID id,
        @JsonProperty("diagnostico_cif") String diagnosticoCif
    ) {}
}
