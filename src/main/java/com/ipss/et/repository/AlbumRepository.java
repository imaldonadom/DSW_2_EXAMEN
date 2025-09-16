package com.ipss.et.repository;

import com.ipss.et.model.Album;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AlbumRepository extends JpaRepository<Album, Long> {

    // Lista con categoría y tags para evitar LazyInitialization
    @EntityGraph(attributePaths = {"categoria", "tags"})
    @Query("select a from Album a")
    List<Album> findAllGraph();

    // Un álbum con categoría y tags
    @EntityGraph(attributePaths = {"categoria", "tags"})
    @Query("select a from Album a where a.id = :id")
    Optional<Album> findByIdGraph(@Param("id") Long id);

    // ===== NUEVO: álbumes que tienen al menos una lámina =====
    @Query("""
           select distinct a
           from Album a
           where exists (
             select 1 from Lamina l
             where l.album = a
           )
           order by a.nombre asc
           """)
    List<Album> findAllWithLaminas();
}
