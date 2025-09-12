package com.ipss.et.repository;

import com.ipss.et.model.Album;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AlbumRepository extends JpaRepository<Album, Long> {

    // Carga categoría y tags para evitar LazyInitializationException en DTO
    @EntityGraph(attributePaths = {"categoria", "tags"})
    @Query("select a from Album a")
    List<Album> findAllGraph();

    @EntityGraph(attributePaths = {"categoria", "tags"})
    @Query("select a from Album a where a.id = :id")
    Optional<Album> findByIdGraph(@Param("id") Long id);
}
