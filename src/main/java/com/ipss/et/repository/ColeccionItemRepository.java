// src/main/java/com/ipss/et/repository/ColeccionItemRepository.java
package com.ipss.et.repository;

import com.ipss.et.model.ColeccionItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ColeccionItemRepository extends JpaRepository<ColeccionItem, Long> {

    /* ============================
       Métodos con los NOMBRES que tu Service espera
       ============================ */

    @Query("""
           select ci
             from ColeccionItem ci
            where ci.coleccionistaId = :coleccionistaId
              and ci.album.id       = :albumId
           """)
    List<ColeccionItem> findByColeccionistaIdAndAlbumId(@Param("coleccionistaId") Long coleccionistaId,
                                                        @Param("albumId") Long albumId);

    @Query("""
           select ci
             from ColeccionItem ci
            where ci.coleccionistaId = :coleccionistaId
              and ci.album.id       = :albumId
              and ci.lamina.id      = :laminaId
           """)
    Optional<ColeccionItem> findByColeccionistaIdAndAlbumIdAndLaminaId(@Param("coleccionistaId") Long coleccionistaId,
                                                                       @Param("albumId") Long albumId,
                                                                       @Param("laminaId") Long laminaId);

    @Modifying
    @Query("""
           delete
             from ColeccionItem ci
            where ci.coleccionistaId = :coleccionistaId
              and ci.album.id       = :albumId
           """)
    int deleteByColeccionistaIdAndAlbumId(@Param("coleccionistaId") Long coleccionistaId,
                                          @Param("albumId") Long albumId);

    /* ============================
       (Opcionales) alias que propuse antes — por si los usas en Controller
       ============================ */

    @Query("""
           select ci
             from ColeccionItem ci
            where ci.coleccionistaId = :coleccionistaId
              and ci.album.id       = :albumId
           """)
    List<ColeccionItem> listByCollectorAndAlbum(@Param("coleccionistaId") Long coleccionistaId,
                                                @Param("albumId") Long albumId);

    @Query("""
           select ci
             from ColeccionItem ci
            where ci.coleccionistaId = :coleccionistaId
              and ci.album.id       = :albumId
              and ci.lamina.id      = :laminaId
           """)
    Optional<ColeccionItem> findOneByCollectorAlbumAndLamina(@Param("coleccionistaId") Long coleccionistaId,
                                                             @Param("albumId") Long albumId,
                                                             @Param("laminaId") Long laminaId);

    @Modifying
    @Query("""
           delete
             from ColeccionItem ci
            where ci.coleccionistaId = :coleccionistaId
              and ci.album.id       = :albumId
           """)
    int deleteByCollectorAndAlbum(@Param("coleccionistaId") Long coleccionistaId,
                                  @Param("albumId") Long albumId);
}
