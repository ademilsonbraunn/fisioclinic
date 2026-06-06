package com.fisioclinic.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * PlanoTratamentoResponse — Saída da API para leitura do plano (Módulo 3)
 * ─────────────────────────────────────────────────────────────────────────────
 * Camada: DTO (Data Transfer Object — response)
 *
 * AnamneseResumo: inclui apenas id, dataAvaliacao e queixaPrincipal —
 *   suficiente para o prontuário exibir o vínculo com a avaliação de origem
 *   sem precisar carregar todos os campos da anamnese.
 * ─────────────────────────────────────────────────────────────────────────────
 */
public record PlanoTratamentoResponse(

    UUID id,

    PacienteResumo paciente,

    AnamneseResumo anamnese,

    FisioterapeutaResumo fisioterapeuta,

    @JsonProperty("diagnostico_cif")
    String diagnosticoCif,

    @JsonProperty("cid10")
    String cid10,

    @JsonProperty("objetivos_curto_prazo")
    String objetivosCurtoPrazo,

    @JsonProperty("objetivos_longo_prazo")
    String objetivosLongoPrazo,

    List<String> tecnicas,

    @JsonProperty("frequencia_semanal")
    Integer frequenciaSemanal,

    @JsonProperty("num_sessoes_estimado")
    Integer numSessoesEstimado,

    @JsonProperty("hipoteses_tratamento")
    String hipotesesTratamento,

    @JsonProperty("data_inicio")
    LocalDate dataInicio,

    @JsonProperty("data_previsao_alta")
    LocalDate dataPrevisaoAlta,

    String status,

    @JsonProperty("created_at")
    LocalDateTime createdAt

) {
    public record PacienteResumo(
        UUID id,
        @JsonProperty("nome_completo") String nomeCompleto,
        String cpf
    ) {}

    public record AnamneseResumo(
        UUID id,
        @JsonProperty("data_avaliacao") LocalDate dataAvaliacao,
        @JsonProperty("queixa_principal") String queixaPrincipal
    ) {}

    public record FisioterapeutaResumo(
        UUID id,
        String nome,
        String crf
    ) {}
}
