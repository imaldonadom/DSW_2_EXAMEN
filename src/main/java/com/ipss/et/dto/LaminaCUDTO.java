package com.ipss.et.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LaminaCUDTO {
    private Long id;          // para update si alguna vez lo usas
    private Integer numero;
    private Long albumId;
    private Long tipoId;      // opcional
}
