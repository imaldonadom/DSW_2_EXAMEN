package com.ipss.et.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class AlbumCUDTO {
    private String nombre;
    private Long categoriaId;           // Opción A
    private List<String> tags;
    private Boolean activo;             // default true si viene null
    private LocalDate fechaLanzamiento; // acepta yyyy-MM-dd o dd-MM-yyyy
    private LocalDate fechaSorteo;
    private Integer cantidadLaminas;    // default 0 si viene null
}
