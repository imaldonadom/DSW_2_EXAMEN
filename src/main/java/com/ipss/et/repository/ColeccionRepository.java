package com.ipss.et.repository;

import com.ipss.et.model.Coleccion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ColeccionRepository extends JpaRepository<Coleccion, Long> {
    Optional<Coleccion> findByColeccionistaIdAndAlbumId(Long coleccionistaId, Long albumId);
}
