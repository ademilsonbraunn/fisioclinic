package com.fisioclinic.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * AuditoriaResponse — Saída da API para eventos de auditoria do prontuário (P2)
 * dados_novos intencionalmente ausente — dados clínicos sensíveis (LGPD art. 11).
 * Expostos apenas para uso interno do AuditoriaService.
 */
public record AuditoriaResponse(

    UUID id,

    @JsonProperty("tipo_entidade")
    String tipoEntidade,

    @JsonProperty("entidade_id")
    UUID entidadeId,

    String acao,

    @JsonProperty("fisioterapeuta_nome")
    String fisioterapeutaNome,

    @JsonProperty("created_at")
    LocalDateTime createdAt

) {}
