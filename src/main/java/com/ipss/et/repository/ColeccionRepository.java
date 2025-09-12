package com.ipss.et.repository;

import com.ipss.et.model.Coleccion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ColeccionRepository extends JpaRepository<Coleccion, Long> {
    List<Coleccion> findByColeccionistaId(Long coleccionistaId);
}
