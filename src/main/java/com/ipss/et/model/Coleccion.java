package com.ipss.et.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity @Table(name="colecciones")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Coleccion {

    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="coleccionista_id", nullable=false)
    private Coleccionista coleccionista;

    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="album_id", nullable=false)
    private Album album;

    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="estado_id")
    private Estado estado;

    @Column(nullable=false)
    @Builder.Default
    private Boolean activo = true;

    @OneToMany(mappedBy="coleccion", cascade=CascadeType.ALL, orphanRemoval=true)
    @Builder.Default
    private List<ColeccionItem> laminas = new ArrayList<>();
}
