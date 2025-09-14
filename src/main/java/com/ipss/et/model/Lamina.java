package com.ipss.et.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "laminas")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Lamina {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer numero;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "album_id", nullable = false)
    private Album album;

    // Opcional
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "tipo_id")
    private TipoLamina tipo;
}
