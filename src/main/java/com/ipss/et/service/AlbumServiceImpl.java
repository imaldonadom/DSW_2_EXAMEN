package com.ipss.et.service;

import com.ipss.et.dto.AlbumCUDTO;
import com.ipss.et.dto.AlbumDTO;
import com.ipss.et.model.Album;
import com.ipss.et.model.Categoria;
import com.ipss.et.repository.AlbumRepository;
import com.ipss.et.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AlbumServiceImpl implements AlbumService {

    private final AlbumRepository albums;
    private final CategoriaRepository categorias;

    @Transactional(readOnly = true)
    public List<AlbumDTO> list() {
        return albums.findAllGraph().stream().map(AlbumDTO::of).toList();
    }

    @Transactional
    public AlbumDTO create(AlbumCUDTO in) {
        Album a = new Album();
        apply(a, in);
        return AlbumDTO.of(albums.save(a));
    }

    @Transactional
    public AlbumDTO update(Long id, AlbumCUDTO in) {
        Album a = albums.findByIdGraph(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Álbum no existe"));
        apply(a, in);
        return AlbumDTO.of(albums.save(a));
    }

    @Transactional
    public void delete(Long id) {
        if (!albums.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Álbum no existe");
        }
        try {
            albums.deleteById(id);
        } catch (DataIntegrityViolationException ex) {
            // FK u otra restricción: devuelvo 409
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede eliminar: el álbum tiene datos relacionados");
        }
    }

    // ---- helpers ----
    private void apply(Album a, AlbumCUDTO in) {
        a.setNombre(in.getNombre());
        a.setFechaLanzamiento(in.getFechaLanzamiento());
        a.setFechaSorteo(in.getFechaSorteo());
        a.setActivo(in.getActivo() != null ? in.getActivo() : Boolean.TRUE);
        a.setCantidadLaminas(in.getCantidadLaminas() != null ? in.getCantidadLaminas() : 0);
        a.setTags(in.getTags() != null ? in.getTags() : List.of());

        Long catId = in.getCategoriaId();
        if (catId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Categoría requerida");
        }
        Categoria cat = categorias.findById(catId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Categoría inválida"));
        a.setCategoria(cat);
    }
}
