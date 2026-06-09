package com.fisioclinic.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * ArquivoAnamneseResponse — Saída da API para arquivos da anamnese (Módulo 2)
 * ─────────────────────────────────────────────────────────────────────────────
 * O campo 'url' retorna o endpoint de download autenticado:
 *   /api/anamneses/{anamneseId}/arquivos/{id}/download
 * O frontend usa esta URL para baixar o arquivo enviando o token JWT.
 * ─────────────────────────────────────────────────────────────────────────────
 */
public record ArquivoAnamneseResponse(

    UUID id,

    String tipo,

    @JsonProperty("nome_arquivo")
    String nomeArquivo,

    String url,

    @JsonProperty("tamanho_bytes")
    Long tamanhoBytes,

    @JsonProperty("created_at")
    LocalDateTime createdAt

) {}
