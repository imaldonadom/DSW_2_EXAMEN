package com.ipss.et.repository;

import com.ipss.et.model.Lamina;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LaminaRepository extends JpaRepository<Lamina, Long> {
    Optional<Lamina> findByAlbumIdAndNumero(Long albumId, Integer numero);
    long countByAlbumId(Long albumId);
}
