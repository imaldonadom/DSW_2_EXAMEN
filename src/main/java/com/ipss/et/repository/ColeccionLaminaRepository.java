package com.ipss.et.repository;

import com.ipss.et.model.ColeccionLamina;
import com.ipss.et.model.Album;
import com.ipss.et.model.Lamina;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ColeccionLaminaRepository extends JpaRepository<ColeccionLamina, Long> {

    Optional<ColeccionLamina> findByColeccionistaIdAndAlbumAndLamina(Long coleccionistaId, Album album, Lamina lamina);

    List<ColeccionLamina> findByColeccionistaIdAndAlbum_Id(Long coleccionistaId, Long albumId);

    Integer countByColeccionistaIdAndAlbum_Id(Long coleccionistaId, Long albumId);

}
