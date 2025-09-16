package com.ipss.et.repository;

import com.ipss.et.model.Lamina;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface LaminaRepository extends JpaRepository<Lamina, Long> {

    @EntityGraph(attributePaths = {"album", "tipo"})   // <-- TRAE album y tipo con fetch-eager
    List<Lamina> findByAlbumIdOrderByNumeroAsc(Long albumId);

    boolean existsByAlbumIdAndNumero(Long albumId, Integer numero);

    long countByAlbumId(Long albumId);

    Optional<Lamina> findByAlbumIdAndNumero(Long albumId, Integer numero);

    void deleteByAlbumId(Long albumId);

    List<Lamina> findAllByIdIn(Collection<Long> ids);

    @Modifying
    @Query("delete from Lamina l where l.id in ?1")
    int deleteByIdIn(Collection<Long> ids);
}
