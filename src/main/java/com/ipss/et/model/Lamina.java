package com.ipss.et.model;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="laminas",
        uniqueConstraints = @UniqueConstraint(name="uk_album_numero", columnNames = {"album_id","numero"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Lamina {

    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private Integer numero;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="tipo_lamina_id")
    private TipoLamina tipoLamina; // p.ej., Normal/Brillante

    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="album_id", nullable=false)
    private Album album;
}
