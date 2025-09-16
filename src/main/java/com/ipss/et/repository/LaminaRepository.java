package com.ipss.et.repository;

import com.ipss.et.model.Lamina;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface LaminaRepository extends JpaRepository<Lamina, Long> {

    List<Lamina> findByAlbumIdOrderByNumeroAsc(Long albumId);

    boolean existsByAlbumIdAndNumero(Long albumId, Integer numero);

    long countByAlbumId(Long albumId);

    /** Recupera una lámina por albumId + numero (para altas unitarias/masivas) */
    Optional<Lamina> findByAlbumIdAndNumero(Long albumId, Integer numero);

    /** borra todas las láminas de un álbum */
    void deleteByAlbumId(Long albumId);

    /** para saber cuántas existen antes de borrar por selección */
    List<Lamina> findAllByIdIn(Collection<Long> ids);

    /** borrado masivo por selección (más eficiente) */
    @Modifying
    @Query("delete from Lamina l where l.id in ?1")
    int deleteByIdIn(Collection<Long> ids);
}
