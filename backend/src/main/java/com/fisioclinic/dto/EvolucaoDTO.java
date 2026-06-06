package com.fisioclinic.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * EvolucaoDTO — Entrada para registro de evolução SOAP (Módulo 5)
 * ─────────────────────────────────────────────────────────────────────────────
 * Camada: DTO (Data Transfer Object — request)
 *
 * Os quatro campos SOAP são obrigatórios (subjetivo, objetivo, avaliacao,
 * planoEvolucao) — exigência do CFM para prontuário eletrônico.
 * evaAntes / evaDepois: escala 0–10 (EVA), validada com @Min/@Max.
 * dataHora: se null, o Service assume LocalDateTime.now() no momento do POST.
 * Nunca logar este DTO — contém dados clínicos sensíveis (LGPD art. 11).
 * ─────────────────────────────────────────────────────────────────────────────
 */
public record EvolucaoDTO(

    @NotNull(message = "sessao_id é obrigatório")
    @JsonProperty("sessao_id")
    UUID sessaoId,

    @JsonProperty("fisioterapeuta_id")
    UUID fisioterapeutaId,

    @JsonProperty("plano_tratamento_id")
    UUID planoTratamentoId,

    @NotNull(message = "num_sessao é obrigatório")
    @JsonProperty("num_sessao")
    Integer numSessao,

    @JsonProperty("data_hora")
    LocalDateTime dataHora,

    @JsonProperty("tempo_atendimento_min")
    Integer tempoAtendimentoMin,

    @NotBlank(message = "subjetivo é obrigatório")
    String subjetivo,

    @NotBlank(message = "objetivo é obrigatório")
    String objetivo,

    @NotBlank(message = "avaliacao é obrigatória")
    String avaliacao,

    @NotBlank(message = "plano é obrigatório")
    @JsonProperty("plano_evolucao")
    String planoEvolucao,

    @JsonProperty("tecnicas_realizadas")
    List<String> tecnicasRealizadas,

    String aparelhos,

    @Min(0) @Max(10)
    @JsonProperty("eva_antes")
    Integer evaAntes,

    @Min(0) @Max(10)
    @JsonProperty("eva_depois")
    Integer evaDepois,

    @JsonProperty("codigo_tuss")
    String codigoTuss,

    String observacoes

) {}
