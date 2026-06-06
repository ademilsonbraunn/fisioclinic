package com.fisioclinic.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;
import java.util.UUID;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * AnamneseDTO — Entrada para criação de anamnese (Módulo 2)
 * ─────────────────────────────────────────────────────────────────────────────
 * Camada: DTO (Data Transfer Object — request)
 *
 * avaliacaoFisica: Map<String, Object> serializado como JSONB no banco.
 *   Estrutura esperada (não validada pelo Bean Validation — validação no Service):
 *   { "postura": "...", "adm": "...", "forca_muscular": "...",
 *     "eva": 0-10, "testes_especiais": [], "goniometria": "..." }
 *
 * Dados clínicos são sensíveis pela LGPD art. 11 — nunca logar este DTO.
 * ─────────────────────────────────────────────────────────────────────────────
 */
public record AnamneseDTO(

    @NotNull(message = "Paciente é obrigatório")
    @JsonProperty("paciente_id")
    UUID pacienteId,

    @JsonProperty("fisioterapeuta_id")
    UUID fisioterapeutaId,

    @NotBlank(message = "Queixa principal é obrigatória")
    @JsonProperty("queixa_principal")
    String queixaPrincipal,

    @NotBlank(message = "Histórico da doença atual é obrigatório")
    @JsonProperty("historico_doenca_atual")
    String historicoDoencaAtual,

    @JsonProperty("tempo_inicio_sintomas")
    String tempoInicioSintomas,

    @JsonProperty("doencas_preexistentes")
    String doencasPreexistentes,

    @JsonProperty("cirurgias_anteriores")
    String cirurgiasAnteriores,

    String medicamentos,

    String alergias,

    @JsonProperty("historico_familiar")
    String historicoFamiliar,

    String observacoes,

    // { postura, adm, forca_muscular, eva (0-10), testes_especiais[], goniometria }
    @JsonProperty("avaliacao_fisica")
    Map<String, Object> avaliacaoFisica

) {}
