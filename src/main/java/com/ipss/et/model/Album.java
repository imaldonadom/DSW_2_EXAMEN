package com.ipss.et.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity @Table(name="albumes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Album {

    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, length=150)
    private String nombre;

    private LocalDate fechaLanzamiento;
    private LocalDate fechaSorteo;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="categoria_id")
    private Categoria categoria;

    @ElementCollection
    @CollectionTable(name="album_tags", joinColumns=@JoinColumn(name="album_id"))
    @Column(name="tag", length=50)
    @Builder.Default
    private List<String> tags = new ArrayList<>();

    @Column(nullable=false)
    private Integer cantidadLaminas;

    @Column(nullable=false)
    @Builder.Default
    private Boolean activo = true;
}
