package com.ipss.et.model;

import jakarta.persistence.*;

@Entity
@Table(name = "coleccion_item")
public class ColeccionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación que EXIGE mappedBy = "coleccion" en Coleccion.laminas
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coleccion_id", nullable = false)
    private Coleccion coleccion;

    // Si aún no manejas usuarios/autenticación, usa 1L fijo en la capa de servicio
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

    // ===== Getters y Setters =====
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Coleccion getColeccion() { return coleccion; }
    public void setColeccion(Coleccion coleccion) { this.coleccion = coleccion; }

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
