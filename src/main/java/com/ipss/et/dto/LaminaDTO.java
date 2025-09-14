package com.ipss.et.dto;

import com.ipss.et.model.Lamina;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LaminaDTO {
    private Long id;
    private Integer numero;
    private Long albumId;
    private String albumNombre;
    private Long tipoId;
    private String tipoNombre;

    public static LaminaDTO of(Lamina l) {
        return LaminaDTO.builder()
                .id(l.getId())
                .numero(l.getNumero())
                .albumId(l.getAlbum().getId())
                .albumNombre(l.getAlbum().getNombre())
                .tipoId(l.getTipo() != null ? l.getTipo().getId() : null)
                .tipoNombre(l.getTipo() != null ? l.getTipo().getNombre() : "-")
                .build();
    }
}
