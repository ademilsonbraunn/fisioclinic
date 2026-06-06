package com.fisioclinic.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * EvolucaoResponse — Saída da API para leitura de evolução (Módulo 5)
 * ─────────────────────────────────────────────────────────────────────────────
 * Camada: DTO (Data Transfer Object — response)
 *
 * Aninha SessaoResumo, PacienteResumo e FisioterapeutaResumo para que o
 * frontend renderize a linha do tempo do prontuário sem chamadas extras.
 * ─────────────────────────────────────────────────────────────────────────────
 */
public record EvolucaoResponse(

    UUID id,

    SessaoResumo sessao,

    PacienteResumo paciente,

    FisioterapeutaResumo fisioterapeuta,

    @JsonProperty("plano_tratamento_id")
    UUID planoTratamentoId,

    @JsonProperty("num_sessao")
    Integer numSessao,

    @JsonProperty("data_hora")
    LocalDateTime dataHora,

    @JsonProperty("tempo_atendimento_min")
    Integer tempoAtendimentoMin,

    String subjetivo,

    String objetivo,

    String avaliacao,

    @JsonProperty("plano_evolucao")
    String planoEvolucao,

    @JsonProperty("tecnicas_realizadas")
    List<String> tecnicasRealizadas,

    String aparelhos,

    @JsonProperty("eva_antes")
    Integer evaAntes,

    @JsonProperty("eva_depois")
    Integer evaDepois,

    @JsonProperty("codigo_tuss")
    String codigoTuss,

    String observacoes,

    @JsonProperty("created_at")
    LocalDateTime createdAt

) {

    public record SessaoResumo(
        UUID id,
        @JsonProperty("data_hora_inicio") LocalDateTime dataHoraInicio
    ) {}

    public record PacienteResumo(
        UUID id,
        @JsonProperty("nome_completo") String nomeCompleto
    ) {}

    public record FisioterapeutaResumo(
        UUID id,
        String nome,
        String crf
    ) {}
}
