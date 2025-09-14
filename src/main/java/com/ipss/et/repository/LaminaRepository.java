package com.ipss.et.repository;

import com.ipss.et.model.Lamina;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LaminaRepository extends JpaRepository<Lamina, Long> {

    List<Lamina> findByAlbumIdOrderByNumeroAsc(Long albumId);

    List<Lamina> findAllByOrderByAlbumIdAscNumeroAsc();
}
