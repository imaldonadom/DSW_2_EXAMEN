package com.ipss.et.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ipss.et.model.Album;
import com.ipss.et.model.Categoria;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AlbumDTO {

    private Long id;

    @JsonProperty("nombre")
    private String nombre;

    /** Permite enviar un objeto {id, nombre} o solo un id suelto */
    @JsonProperty("categoria")
    private Categoria categoria;

    @JsonProperty("categoriaId")
    @JsonAlias({"categoria_id"})
    private Long categoriaId;

    @JsonProperty("tags")
    private List<String> tags;

    @JsonProperty("activo")
    private Boolean activo;

    @JsonProperty("fecha de lanzamiento")
    @JsonAlias({"fechaLanzamiento", "fecha_lanzamiento"})
    private LocalDate fechaLanzamiento;

    @JsonProperty("fecha sorteo")
    @JsonAlias({"fechaSorteo", "fecha_sorteo"})
    private LocalDate fechaSorteo;

    @JsonProperty("cantidad de láminas")
    @JsonAlias({"cantidadLaminas", "cantidad_laminas"})
    private Integer cantidadLaminas;

    /* -------- Mapeos -------- */

    public static AlbumDTO of(Album a) {
        if (a == null) return null;

        Categoria cat = null;
        if (a.getCategoria() != null) {
            cat = new Categoria();
            cat.setId(a.getCategoria().getId());
            cat.setNombre(a.getCategoria().getNombre());
        }

        List<String> tagList = new ArrayList<>();
        if (a.getTags() != null) tagList.addAll(a.getTags());

        return AlbumDTO.builder()
                .id(a.getId())
                .nombre(a.getNombre())
                .categoria(cat)
                .categoriaId(cat != null ? cat.getId() : null)
                .tags(tagList)
                .activo(Boolean.TRUE.equals(a.getActivo())) // <-- getActivo()
                .fechaLanzamiento(a.getFechaLanzamiento())
                .fechaSorteo(a.getFechaSorteo())
                .cantidadLaminas(a.getCantidadLaminas())
                .build();
    }

    /** Convierte el DTO a entidad. Pásale la categoría ya resuelta (o null). */
    public Album toEntity(Categoria categoriaResolved) {
        Album a = new Album();
        a.setId(this.id);
        a.setNombre(this.nombre);

        Categoria cat = categoriaResolved;
        if (cat == null && this.categoria != null) {
            cat = new Categoria();
            cat.setId(this.categoria.getId());
            cat.setNombre(this.categoria.getNombre());
        }
        a.setCategoria(cat);

        if (this.tags != null) {
            // deduplica manteniendo orden y pasa List<String> (lo que espera la entidad)
            List<String> uniqueTags = new ArrayList<>(new LinkedHashSet<>(this.tags));
            a.setTags(uniqueTags); // <-- List, no Set
        }

        a.setActivo(Boolean.TRUE.equals(this.activo));
        a.setFechaLanzamiento(this.fechaLanzamiento);
        a.setFechaSorteo(this.fechaSorteo);
        a.setCantidadLaminas(this.cantidadLaminas != null ? this.cantidadLaminas : 0);

        return a;
    }
}
