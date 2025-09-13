package com.ipss.et.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "albumes")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Album {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nombre;

    // Aseguramos formato ISO en JSON
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaLanzamiento;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaSorteo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @ElementCollection
    @CollectionTable(name = "album_tags", joinColumns = @JoinColumn(name = "album_id"))
    @Column(name = "tag", length = 50)
    @Builder.Default
    private List<String> tags = new ArrayList<>();

    // IMPORTANTE: nombre de columna + default 0
    @Column(name = "cantidad_laminas", nullable = false)
    @Builder.Default
    private Integer cantidadLaminas = 0;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    // Saneamos por si llega null desde el front
    @PrePersist @PreUpdate
    private void sanitize() {
        if (cantidadLaminas == null) cantidadLaminas = 0;
        if (activo == null) activo = true;
        if (tags == null) tags = new ArrayList<>();
    }
}
