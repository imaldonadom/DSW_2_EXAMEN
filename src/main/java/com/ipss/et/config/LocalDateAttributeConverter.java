package com.ipss.et.config;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Converter(autoApply = true)
public class LocalDateAttributeConverter implements AttributeConverter<LocalDate, String> {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DMY = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    @Override
    public String convertToDatabaseColumn(LocalDate attribute) {
        if (attribute == null) return null;
        return attribute.format(ISO); // siempre guardamos como yyyy-MM-dd (texto)
    }

    @Override
    public LocalDate convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        String v = dbData.trim();
        if (v.isEmpty()) return null;
        try {
            if (v.matches("\\d{4}-\\d{2}-\\d{2}")) return LocalDate.parse(v, ISO);
            if (v.matches("\\d{2}-\\d{2}-\\d{4}")) return LocalDate.parse(v, DMY);
            // último intento (por si llega otro formato)
            return LocalDate.parse(v);
        } catch (Exception ex) {
            // Si algo raro quedó en DB, evita romper el listado
            return null;
        }
    }
}
