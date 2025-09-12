package com.ipss.et.model;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="coleccionistas")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Coleccionista {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, length=120)
    private String nombre;
}
