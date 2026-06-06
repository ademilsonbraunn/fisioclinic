package com.fisioclinic.model.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class StatusPlanoTratamentoConverter
        implements AttributeConverter<StatusPlanoTratamento, String> {

    @Override
    public String convertToDatabaseColumn(StatusPlanoTratamento s) {
        return s == null ? null : s.getValor();
    }

    @Override
    public StatusPlanoTratamento convertToEntityAttribute(String s) {
        return s == null ? null : StatusPlanoTratamento.fromString(s);
    }
}
