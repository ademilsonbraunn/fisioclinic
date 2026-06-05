package com.fisioclinic.dto;

import com.fisioclinic.model.Fisioterapeuta;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada para criação (POST) e atualização parcial (PATCH) de fisioterapeuta.
 * Para POST, use @Valid — campos marcados com @Not* serão validados.
 * Para PATCH, não use @Valid — campos nulos significam "manter valor atual".
 */
public record FisioterapeutaDTO(

    @NotBlank(message = "Nome é obrigatório")
    String nome,

    @NotBlank(message = "CRF é obrigatório")
    @Size(max = 15, message = "CRF deve ter no máximo 15 caracteres")
    String crf,

    @NotBlank(message = "E-mail é obrigatório")
    @Email(message = "E-mail inválido")
    String email,

    String telefone,

    String senha,

    Fisioterapeuta.Perfil perfil,

    Boolean ativo

) {}
