package com.fisioclinic.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fisioclinic.model.AtualizacaoSistema;

import java.time.LocalDate;
import java.util.UUID;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * AtualizacaoSistemaResponse — Saída da API para o card de novidades
 * ─────────────────────────────────────────────────────────────────────────────
 * Camada: DTO (Data Transfer Object — response)
 *
 * Inclui método de fábrica estático from(AtualizacaoSistema) para conversão
 * direta no Service sem precisar de um mapper separado.
 * ─────────────────────────────────────────────────────────────────────────────
 */
public record AtualizacaoSistemaResponse(
        UUID id,
        String titulo,
        String descricao,
        String versao,
        String tipo,
        @JsonProperty("data_lancamento") LocalDate dataLancamento
) {
    public static AtualizacaoSistemaResponse from(AtualizacaoSistema a) {
        return new AtualizacaoSistemaResponse(
                a.getId(),
                a.getTitulo(),
                a.getDescricao(),
                a.getVersao(),
                a.getTipo(),
                a.getDataLancamento()
        );
    }
}
