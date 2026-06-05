package com.fisioclinic.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fisioclinic.model.Sessao;

import java.time.LocalDateTime;
import java.util.UUID;

public record SessaoResponse(

    UUID id,

    PacienteResumo paciente,

    FisioterapeutaResumo fisioterapeuta,

    SalaResumo sala,

    @JsonProperty("data_hora_inicio")
    LocalDateTime dataHoraInicio,

    @JsonProperty("data_hora_fim")
    LocalDateTime dataHoraFim,

    @JsonProperty("tipo_sessao")
    Sessao.TipoSessao tipoSessao,

    Sessao.StatusSessao status,

    String observacoes,

    @JsonProperty("motivo_cancelamento")
    String motivoCancelamento,

    @JsonProperty("created_at")
    LocalDateTime createdAt

) {

    public record PacienteResumo(
        UUID id,
        @JsonProperty("nome_completo") String nomeCompleto,
        String cpf
    ) {}

    public record FisioterapeutaResumo(
        UUID id,
        String nome,
        String crf
    ) {}

    public record SalaResumo(
        UUID id,
        String nome,
        String tipo
    ) {}
}
