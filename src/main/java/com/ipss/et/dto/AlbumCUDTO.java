package com.ipss.et.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AlbumCUDTO {
    private String nombre;

    @JsonProperty("fecha de lanzamiento")
    private LocalDate fechaLanzamiento;

    @JsonProperty("fecha sorteo")
    private LocalDate fechaSorteo;

    private Map<String, Long> categoria; // { "id": 1 }
    private List<String> tags;

    @JsonProperty("cantidad de láminas")
    private Integer cantidadLaminas;
}
