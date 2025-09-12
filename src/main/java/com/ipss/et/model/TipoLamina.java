package com.ipss.et.model;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="tipos_lamina")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TipoLamina {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, unique=true, length=120)
    private String nombre;
}
