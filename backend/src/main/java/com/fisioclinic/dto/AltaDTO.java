package com.fisioclinic.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * AltaDTO — Entrada para registro de alta do paciente (Módulo 6)
 * ─────────────────────────────────────────────────────────────────────────────
 * Camada: DTO (Data Transfer Object — request)
 *
 * paciente_id e resultado_objetivos são os únicos campos obrigatórios além
 * do motivo — os demais (relatórios, satisfação, retorno) são opcionais.
 *
 * data_alta: se null, o AltaService assume LocalDate.now().
 * satisfacao_nota: validado entre 1 e 5 quando informado.
 *
 * Nunca logar este DTO — contém dados clínicos sensíveis (LGPD art. 11).
 * ─────────────────────────────────────────────────────────────────────────────
 */
public record AltaDTO(

    @NotNull(message = "paciente_id é obrigatório")
    @JsonProperty("paciente_id")
    UUID pacienteId,

    @JsonProperty("plano_id")
    UUID planoId,

    @JsonProperty("fisioterapeuta_id")
    UUID fisioterapeutaId,

    @JsonProperty("data_alta")
    LocalDate dataAlta,

    @NotBlank(message = "motivo é obrigatório")
    String motivo,

    @NotBlank(message = "resultado_objetivos é obrigatório")
    @JsonProperty("resultado_objetivos")
    String resultadoObjetivos,

    @JsonProperty("orientacoes_domiciliares")
    String orientacoesDomiciliares,

    @JsonProperty("relatorio_evolucao")
    String relatorioEvolucao,

    @JsonProperty("relatorio_medico")
    String relatorioMedico,

    @JsonProperty("agendamento_retorno")
    LocalDate agendamentoRetorno,

    @Min(1) @Max(5)
    @JsonProperty("satisfacao_nota")
    Integer satisfacaoNota,

    @JsonProperty("satisfacao_comentario")
    String satisfacaoComentario

) {}
