package com.ipss.et.repository;

import com.ipss.et.model.Lamina;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LaminaRepository extends JpaRepository<Lamina, Long> {

    boolean existsByAlbumIdAndNumero(Long albumId, Integer numero);

    List<Lamina> findByAlbumIdOrderByNumeroAsc(Long albumId);

    Optional<Lamina> findByAlbumIdAndNumero(Long albumId, Integer numero);
}
