package com.ipss.et.model;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="estados")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Estado {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, unique=true, length=80)
    private String nombre; // "incompleto", "completo", etc.
}
