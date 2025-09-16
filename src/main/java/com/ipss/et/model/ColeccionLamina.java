package com.ipss.et.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "coleccion_lamina",
    uniqueConstraints = {
        // Un mismo coleccionista puede tener varias de la misma lámina: se
        // consolida por (coleccionistaId, album_id, lamina_id)
        @UniqueConstraint(name = "uk_coleccion_lamina_colec_album_lamina",
            columnNames = {"coleccionista_id", "album_id", "lamina_id"})
    }
)
public class ColeccionLamina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // si aún no manejas auth/usuarios, usa valor fijo (p.ej. 1L) desde el front
    @Column(name = "coleccionista_id", nullable = false)
    private Long coleccionistaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "album_id", nullable = false)
    private Album album;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lamina_id", nullable = false)
    private Lamina lamina;

    @Column(nullable = false)
    private Integer cantidad = 0;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public ColeccionLamina() {}

    public ColeccionLamina(Long coleccionistaId, Album album, Lamina lamina, Integer cantidad) {
        this.coleccionistaId = coleccionistaId;
        this.album = album;
        this.lamina = lamina;
        this.cantidad = cantidad == null ? 0 : cantidad;
    }

    public Long getId() { return id; }
    public Long getColeccionistaId() { return coleccionistaId; }
    public void setColeccionistaId(Long coleccionistaId) { this.coleccionistaId = coleccionistaId; }

    public Album getAlbum() { return album; }
    public void setAlbum(Album album) { this.album = album; }

    public Lamina getLamina() { return lamina; }
    public void setLamina(Lamina lamina) { this.lamina = lamina; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
