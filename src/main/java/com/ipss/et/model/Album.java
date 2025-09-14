package com.ipss.et.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "albumes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
public class Album {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nombre;

    // Acepta varias formas que podría mandar el front
    @JsonAlias({"fecha_lanzamiento", "fecha de lanzamiento"})
    private LocalDate fechaLanzamiento;

    @JsonAlias({"fecha_sorteo", "fecha sorteo"})
    private LocalDate fechaSorteo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @ElementCollection
    @CollectionTable(name = "album_tags", joinColumns = @JoinColumn(name = "album_id"))
    @Column(name = "tag", length = 50)
    @Builder.Default
    private List<String> tags = new ArrayList<>();

    @Column(nullable = false)
    @JsonAlias({"cantidad_laminas", "cantidadlaminas", "cantidad de láminas"})
    private Integer cantidadLaminas;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @PrePersist @PreUpdate
    private void ensureDefaults() {
        if (tags == null) tags = new ArrayList<>();
        if (activo == null) activo = true;
        if (cantidadLaminas == null) cantidadLaminas = 0;
    }
}
