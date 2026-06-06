package com.fisioclinic.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fisioclinic.model.Sessao;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * SessaoDTO — Entrada para criação e atualização de sessão (Módulo 4)
 * ─────────────────────────────────────────────────────────────────────────────
 * Camada: DTO (Data Transfer Object — request)
 *
 * Todos os campos usam @JsonProperty com snake_case para compatibilidade com
 * o frontend JavaScript (convenção camelCase do Java → snake_case do JS).
 * tipoSessao e status recebem os valores do enum como string JSON
 *   (ex: "SESSAO", "AGENDADO") — Jackson faz a deserialização automática.
 * ─────────────────────────────────────────────────────────────────────────────
 */
public record SessaoDTO(

    @JsonProperty("paciente_id")
    @NotNull
    UUID pacienteId,

    @JsonProperty("fisioterapeuta_id")
    @NotNull
    UUID fisioterapeutaId,

    @JsonProperty("sala_id")
    @NotNull
    UUID salaId,

    @JsonProperty("data_hora_inicio")
    @NotNull
    LocalDateTime dataHoraInicio,

    @JsonProperty("data_hora_fim")
    @NotNull
    LocalDateTime dataHoraFim,

    @JsonProperty("tipo_sessao")
    @NotNull
    Sessao.TipoSessao tipoSessao,

    @JsonProperty("status")
    @NotNull
    Sessao.StatusSessao status,

    @JsonProperty("observacoes")
    String observacoes,

    @JsonProperty("motivo_cancelamento")
    String motivoCancelamento

) {}
