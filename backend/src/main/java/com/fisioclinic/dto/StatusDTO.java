package com.fisioclinic.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fisioclinic.model.Sessao;
import jakarta.validation.constraints.NotNull;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * StatusDTO — Entrada para PATCH /api/sessoes/{id}/status
 * ─────────────────────────────────────────────────────────────────────────────
 * Camada: DTO (Data Transfer Object — request)
 *
 * Record mínimo para a operação mais frequente da agenda: mudar o status de
 * uma sessão sem precisar reenviar todos os dados do agendamento.
 * motivoCancelamento é obrigatório somente quando status = CANCELADO —
 * essa validação condicional é feita no SessaoService, não com Bean Validation.
 * ─────────────────────────────────────────────────────────────────────────────
 */
public record StatusDTO(

    @JsonProperty("status")
    @NotNull
    Sessao.StatusSessao status,

    @JsonProperty("motivo_cancelamento")
    String motivoCancelamento

) {}
