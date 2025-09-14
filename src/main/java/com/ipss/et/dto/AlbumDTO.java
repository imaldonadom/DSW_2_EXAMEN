package com.ipss.et.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class AlbumDTO {
    private Long id;
    private String nombre;
    private Cat categoria;
    private List<String> tags;
    private Boolean activo;
    private LocalDate fechaLanzamiento;
    private LocalDate fechaSorteo;
    private Integer cantidadLaminas;

    @Data
    public static class Cat {
        private Long id;
        private String nombre;
    }
}
