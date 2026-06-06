package com.fisioclinic.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * AltaResponse — Saída da API para leitura de alta (Módulo 6)
 * ─────────────────────────────────────────────────────────────────────────────
 * Camada: DTO (Data Transfer Object — response)
 *
 * Aninha PacienteResumo, PlanoResumo e FisioterapeutaResumo para que o
 * frontend renderize o resumo da alta sem chamadas extras.
 * ─────────────────────────────────────────────────────────────────────────────
 */
public record AltaResponse(

    UUID id,

    PacienteResumo paciente,

    PlanoResumo plano,

    FisioterapeutaResumo fisioterapeuta,

    @JsonProperty("data_alta")
    LocalDate dataAlta,

    String motivo,

    @JsonProperty("resultado_objetivos")
    String resultadoObjetivos,

    @JsonProperty("orientacoes_domiciliares")
    String orientacoesDomiciliares,

    @JsonProperty("relatorio_evolucao")
    String relatorioEvolucao,

    @JsonProperty("relatorio_medico")
    String relatorioMedico,

    @JsonProperty("num_sessoes_realizadas")
    Integer numSessoesRealizadas,

    @JsonProperty("agendamento_retorno")
    LocalDate agendamentoRetorno,

    @JsonProperty("satisfacao_nota")
    Integer satisfacaoNota,

    @JsonProperty("satisfacao_comentario")
    String satisfacaoComentario,

    @JsonProperty("created_at")
    LocalDateTime createdAt

) {

    public record PacienteResumo(
        UUID id,
        @JsonProperty("nome_completo") String nomeCompleto,
        String cpf
    ) {}

    public record PlanoResumo(
        UUID id,
        @JsonProperty("diagnostico_cif") String diagnosticoCif,
        String status
    ) {}

    public record FisioterapeutaResumo(
        UUID id,
        String nome,
        String crf
    ) {}
}
