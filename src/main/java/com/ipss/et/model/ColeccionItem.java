package com.ipss.et.model;

import jakarta.persistence.*;

@Entity
@Table(name = "coleccion_item")
public class ColeccionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Si aún no manejas usuarios/autenticación, usa 1L fijo
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

    @Column(nullable = false)
    private Boolean activo = true;

    // ----- getters / setters -----
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getColeccionistaId() { return coleccionistaId; }
    public void setColeccionistaId(Long coleccionistaId) { this.coleccionistaId = coleccionistaId; }

    public Album getAlbum() { return album; }
    public void setAlbum(Album album) { this.album = album; }

    public Lamina getLamina() { return lamina; }
    public void setLamina(Lamina lamina) { this.lamina = lamina; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
}
