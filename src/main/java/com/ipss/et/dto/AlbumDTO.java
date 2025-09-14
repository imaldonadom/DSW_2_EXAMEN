package com.ipss.et.dto;

import com.ipss.et.model.Album;
import com.ipss.et.model.Categoria;
import com.ipss.et.repository.CategoriaRepository;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlbumDTO {

    private Long id;
    private String nombre;
    private Cat categoria;                 // <- lo que el front necesita
    private Boolean activo;
    private Integer cantidadLaminas;
    private LocalDate fechaLanzamiento;
    private LocalDate fechaSorteo;
    private List<String> tags;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Cat {
        private Long id;
        private String nombre;
    }

    public static AlbumDTO of(Album a) {
        return AlbumDTO.builder()
                .id(a.getId())
                .nombre(a.getNombre())
                .categoria(a.getCategoria() == null ? null :
                        new Cat(a.getCategoria().getId(), a.getCategoria().getNombre()))
                .activo(Boolean.TRUE.equals(a.getActivo()))
                .cantidadLaminas(a.getCantidadLaminas() == null ? 0 : a.getCantidadLaminas())
                .fechaLanzamiento(a.getFechaLanzamiento())
                .fechaSorteo(a.getFechaSorteo())
                .tags(a.getTags() == null ? List.of() : a.getTags())
                .build();
    }

    public static Album toEntity(AlbumDTO dto, CategoriaRepository categoriaRepo) {
        Album a = new Album();
        a.setId(dto.getId());
        a.setNombre(dto.getNombre());
        a.setActivo(dto.getActivo() != null ? dto.getActivo() : Boolean.TRUE);
        a.setCantidadLaminas(dto.getCantidadLaminas() != null ? dto.getCantidadLaminas() : 0);
        a.setFechaLanzamiento(dto.getFechaLanzamiento());
        a.setFechaSorteo(dto.getFechaSorteo());
        a.setTags(dto.getTags() != null ? dto.getTags() : new ArrayList<>());

        if (dto.getCategoria() != null && dto.getCategoria().getId() != null) {
            Categoria cat = categoriaRepo.findById(dto.getCategoria().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Categoría inválida"));
            a.setCategoria(cat);
        } else {
            a.setCategoria(null);
        }
        return a;
    }
}
