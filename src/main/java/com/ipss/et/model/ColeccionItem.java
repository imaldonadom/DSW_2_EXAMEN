package com.ipss.et.model;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="coleccion_items")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ColeccionItem {

    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="coleccion_id", nullable=false)
    private Coleccion coleccion;

    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="lamina_id", nullable=false)
    private Lamina lamina;

    @Column(nullable=false)
    private Integer cantidad; // cuántas tiene el coleccionista
}
