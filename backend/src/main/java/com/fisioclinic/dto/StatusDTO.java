package com.fisioclinic.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fisioclinic.model.Sessao;
import jakarta.validation.constraints.NotNull;

public record StatusDTO(

    @JsonProperty("status")
    @NotNull
    Sessao.StatusSessao status,

    @JsonProperty("motivo_cancelamento")
    String motivoCancelamento

) {}
