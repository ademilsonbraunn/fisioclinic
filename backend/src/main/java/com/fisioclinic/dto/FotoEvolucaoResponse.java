package com.fisioclinic.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * FotoEvolucaoResponse — Saída da API para fotos comparativas da evolução (M5 P2)
 * O campo 'url' retorna o endpoint de download autenticado:
 *   /api/evolucoes/{evolucaoId}/fotos/{id}/arquivo
 * O frontend usa esta URL para exibir a foto enviando o token JWT.
 */
public record FotoEvolucaoResponse(

    UUID id,

    @JsonProperty("evolucao_id")
    UUID evolucaoId,

    String tipo,

    @JsonProperty("nome_arquivo")
    String nomeArquivo,

    // URL de acesso autenticado — nunca expõe o caminho real no disco (LGPD art. 11)
    String url,

    @JsonProperty("tamanho_bytes")
    Long tamanhoBytes,

    String descricao,

    @JsonProperty("created_at")
    LocalDateTime createdAt

) {}
