package com.ipss.et.config;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Converter(autoApply = true)
public class LocalDateStringConverter implements AttributeConverter<LocalDate, String> {
    private static final DateTimeFormatter F = DateTimeFormatter.ISO_LOCAL_DATE; // yyyy-MM-dd

    @Override
    public String convertToDatabaseColumn(LocalDate attribute) {
        return (attribute == null) ? null : F.format(attribute);
    }

    @Override
    public LocalDate convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) return null;
        return LocalDate.parse(dbData, F);
    }
}
