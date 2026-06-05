package com.fisioclinic.dto;

import com.fisioclinic.model.Sala;

import java.util.UUID;

public record SalaResponse(

    UUID id,
    String nome,
    Sala.TipoSala tipo,
    Integer capacidade,
    Boolean ativo

) {}
