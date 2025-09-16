package com.ipss.et.repository;

import com.ipss.et.model.Album;
import com.ipss.et.model.ColeccionLamina;
import com.ipss.et.model.Lamina;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ColeccionLaminaRepository extends JpaRepository<ColeccionLamina, Long> {

    /** Carga todo listo para serializar (evita LazyInit): album, lamina y el tipo de la lámina */
    @EntityGraph(attributePaths = {"album", "lamina", "lamina.tipo"})
    List<ColeccionLamina> findByColeccionistaIdAndAlbum_Id(Long coleccionistaId, Long albumId);

    Optional<ColeccionLamina> findByColeccionistaIdAndAlbumAndLamina(Long coleccionistaId,
                                                                      Album album,
                                                                      Lamina lamina);
}
