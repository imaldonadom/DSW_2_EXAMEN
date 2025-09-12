package com.ipss.et.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ipss.et.model.Album;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AlbumDTO {
    Long id;
    String nombre;

    @JsonProperty("fecha de lanzamiento")
    LocalDate fechaLanzamiento;

    @JsonProperty("fecha sorteo")
    LocalDate fechaSorteo;

    Map<String, Object> categoria; // {id, nombre}
    List<String> tags;

    @JsonProperty("cantidad de láminas")
    Integer cantidadLaminas;

    Boolean activo;

    public static AlbumDTO of(Album a) {
        Map<String,Object> cat = (a.getCategoria() == null)
                ? null
                : Map.<String,Object>of(
                    "id", a.getCategoria().getId(),
                    "nombre", a.getCategoria().getNombre()
                  );

        return AlbumDTO.builder()
                .id(a.getId())
                .nombre(a.getNombre())
                .fechaLanzamiento(a.getFechaLanzamiento())
                .fechaSorteo(a.getFechaSorteo())
                .categoria(cat)
                .tags(a.getTags())
                .cantidadLaminas(a.getCantidadLaminas())
                .activo(a.getActivo())
                .build();
    }
}
