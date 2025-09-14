package com.ipss.et.repository;

import com.ipss.et.model.Album;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AlbumRepository extends JpaRepository<Album, Long> {

    // Trae categoría y tags para evitar LazyInitializationException
    @EntityGraph(attributePaths = {"categoria", "tags"})
    @Query("select a from Album a")
    List<Album> findAllWithCategoriaAndTags();

    @EntityGraph(attributePaths = {"categoria", "tags"})
    @Query("select a from Album a where a.id = :id")
    Optional<Album> findByIdWithCategoriaAndTags(@Param("id") Long id);

    // aliases (si en otras partes llaman a estos nombres)
    @EntityGraph(attributePaths = {"categoria", "tags"})
    @Query("select a from Album a")
    default List<Album> findAllGraph() { return findAllWithCategoriaAndTags(); }

    @EntityGraph(attributePaths = {"categoria", "tags"})
    @Query("select a from Album a where a.id = :id")
    default Optional<Album> findByIdGraph(@Param("id") Long id) { return findByIdWithCategoriaAndTags(id); }
}
