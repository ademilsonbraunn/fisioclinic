package com.fisioclinic.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record PlanoTratamentoDTO(

    @NotNull(message = "Paciente é obrigatório")
    @JsonProperty("paciente_id")
    UUID pacienteId,

    @JsonProperty("anamnese_id")
    UUID anamneseId,

    @JsonProperty("fisioterapeuta_id")
    UUID fisioterapeutaId,

    @NotBlank(message = "Diagnóstico fisioterapêutico (CIF) é obrigatório")
    @JsonProperty("diagnostico_cif")
    String diagnosticoCif,

    @JsonProperty("cid10")
    String cid10,

    @NotBlank(message = "Objetivos de curto prazo são obrigatórios")
    @JsonProperty("objetivos_curto_prazo")
    String objetivosCurtoPrazo,

    @NotBlank(message = "Objetivos de longo prazo são obrigatórios")
    @JsonProperty("objetivos_longo_prazo")
    String objetivosLongoPrazo,

    List<String> tecnicas,

    @NotNull(message = "Frequência semanal é obrigatória")
    @Min(value = 1, message = "Frequência mínima: 1 sessão/semana")
    @Max(value = 7, message = "Frequência máxima: 7 sessões/semana")
    @JsonProperty("frequencia_semanal")
    Integer frequenciaSemanal,

    @NotNull(message = "Número de sessões estimado é obrigatório")
    @Min(value = 1, message = "Mínimo de 1 sessão")
    @JsonProperty("num_sessoes_estimado")
    Integer numSessoesEstimado,

    @JsonProperty("hipoteses_tratamento")
    String hipotesesTratamento,

    @JsonProperty("data_inicio")
    LocalDate dataInicio,

    @JsonProperty("data_previsao_alta")
    LocalDate dataPrevisaoAlta,

    String status

) {}
