package com.fisioclinic.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public record AnamneseResponse(

    UUID id,

    PacienteResumo paciente,

    FisioterapeutaResumo fisioterapeuta,

    @JsonProperty("data_avaliacao")
    LocalDate dataAvaliacao,

    @JsonProperty("queixa_principal")
    String queixaPrincipal,

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

    @JsonProperty("avaliacao_fisica")
    Map<String, Object> avaliacaoFisica,

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
}
