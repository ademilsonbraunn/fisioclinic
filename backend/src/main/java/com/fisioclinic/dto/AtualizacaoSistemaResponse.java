package com.fisioclinic.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fisioclinic.model.AtualizacaoSistema;

import java.time.LocalDate;
import java.util.UUID;

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
