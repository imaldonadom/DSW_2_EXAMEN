package com.ipss.et.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Configuration
public class LocalDateStringConverter implements WebMvcConfigurer {
    private static final DateTimeFormatter ISO = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DMY = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    @Override
    public void addFormatters(@NonNull FormatterRegistry registry) {
        registry.addConverter(String.class, LocalDate.class, s -> {
            if (s == null || s.isBlank()) return null;
            String v = s.trim();
            try {
                if (v.matches("\\d{2}-\\d{2}-\\d{4}")) return LocalDate.parse(v, DMY);
                if (v.matches("\\d{4}-\\d{2}-\\d{2}")) return LocalDate.parse(v, ISO);
            } catch (Exception ignore) {}
            return LocalDate.parse(v);
        });
    }
}
