package com.fisioclinic.dto;

import com.fisioclinic.model.Fisioterapeuta;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de saída — nunca expõe senhaHash.
 */
public record FisioterapeutaResponse(

    UUID id,
    String nome,
    String crf,
    String email,
    String telefone,
    Boolean ativo,
    Fisioterapeuta.Perfil perfil,
    LocalDateTime createdAt,
    LocalDateTime updatedAt

) {}
