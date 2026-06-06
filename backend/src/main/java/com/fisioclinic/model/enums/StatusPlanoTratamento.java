package com.fisioclinic.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum StatusPlanoTratamento {
    ATIVO("ativo"), CONCLUIDO("concluido"), CANCELADO("cancelado");

    private final String valor;

    StatusPlanoTratamento(String valor) { this.valor = valor; }

    @JsonValue
    public String getValor() { return valor; }

    @JsonCreator
    public static StatusPlanoTratamento fromString(String s) {
        if (s == null) return null;
        for (StatusPlanoTratamento v : values()) {
            if (v.valor.equalsIgnoreCase(s)) return v;
        }
        throw new IllegalArgumentException(
            "Status inválido: " + s + ". Use: ativo, concluido ou cancelado");
    }
}
