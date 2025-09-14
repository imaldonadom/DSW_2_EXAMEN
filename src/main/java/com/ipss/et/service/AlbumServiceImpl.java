package com.ipss.et.service;

import com.ipss.et.dto.AlbumCUDTO;
import com.ipss.et.dto.AlbumDTO;
import com.ipss.et.model.Album;
import com.ipss.et.model.Categoria;
import com.ipss.et.repository.AlbumRepository;
import com.ipss.et.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AlbumServiceImpl implements AlbumService {

    private final AlbumRepository albumRepository;
    private final CategoriaRepository categoriaRepository;

    /* === OBLIGATORIOS según tu interfaz === */

    @Override
    @Transactional(readOnly = true)
    public List<AlbumDTO> listar() {
        return albumRepository.findAllWithCategoriaAndTags()
                .stream()
                .map(AlbumDTO::of)   // existe el mapper en tu proyecto
                .toList();
    }

    @Override
    @Transactional
    public AlbumDTO crear(AlbumCUDTO in) {
        if (in == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Body vacío");

        Album a = new Album();
        a.setNombre(in.getNombre());
        a.setActivo(in.getActivo() == null ? Boolean.TRUE : in.getActivo());
        a.setCantidadLaminas(in.getCantidadLaminas() == null ? 0 : in.getCantidadLaminas());
        a.setFechaLanzamiento(in.getFechaLanzamiento());
        a.setFechaSorteo(in.getFechaSorteo());
        a.setTags(in.getTags() == null ? new ArrayList<>() : in.getTags());

        if (in.getCategoriaId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Categoría requerida");
        }
        Categoria cat = categoriaRepository.findById(in.getCategoriaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Categoría inválida"));
        a.setCategoria(cat);

        a = albumRepository.save(a);
        return AlbumDTO.of(a);
    }

    /* === Utilitarios opcionales para tu controller (sin @Override por si la interfaz no los define) === */

    @Transactional(readOnly = true)
    public AlbumDTO obtener(Long id) {
        Album a = albumRepository.findByIdWithCategoriaAndTags(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Álbum no existe"));
        return AlbumDTO.of(a);
    }
}
