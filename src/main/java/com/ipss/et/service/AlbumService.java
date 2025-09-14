package com.ipss.et.service;

import com.ipss.et.dto.AlbumCUDTO;
import com.ipss.et.dto.AlbumDTO;
import com.ipss.et.model.Album;
import com.ipss.et.model.Categoria;
import com.ipss.et.repository.AlbumRepository;
import com.ipss.et.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AlbumService {

    private final AlbumRepository albumRepo;
    private final CategoriaRepository categoriaRepo;

    public List<AlbumDTO> list() {
        return albumRepo.findAll().stream().map(this::toDTO).toList();
    }

    public AlbumDTO get(Long id) {
        return toDTO(albumRepo.findById(id).orElseThrow());
    }

    public AlbumDTO create(AlbumCUDTO dto) {
        Album a = new Album();
        apply(a, dto);
        return toDTO(albumRepo.save(a));
    }

    public AlbumDTO update(Long id, AlbumCUDTO dto) {
        Album a = albumRepo.findById(id).orElseThrow();
        apply(a, dto);
        return toDTO(albumRepo.save(a));
    }

    public void delete(Long id) {
        albumRepo.deleteById(id);
    }

    // ---------------- helpers ----------------
    private void apply(Album a, AlbumCUDTO dto) {
        a.setNombre(dto.getNombre());
        a.setFechaLanzamiento(dto.getFechaLanzamiento());
        a.setFechaSorteo(dto.getFechaSorteo());
        a.setTags(dto.getTags() != null ? dto.getTags() : List.of());
        a.setActivo(dto.getActivo() != null ? dto.getActivo() : Boolean.TRUE);
        a.setCantidadLaminas(dto.getCantidadLaminas() != null ? dto.getCantidadLaminas() : 0);

        if (dto.getCategoriaId() == null) {
            throw new IllegalArgumentException("Categoría inválida");
        }
        Categoria cat = categoriaRepo.findById(dto.getCategoriaId())
                .orElseThrow(() -> new IllegalArgumentException("Categoría inválida"));
        a.setCategoria(cat);
    }

    private AlbumDTO toDTO(Album a) {
        AlbumDTO d = new AlbumDTO();
        d.setId(a.getId());
        d.setNombre(a.getNombre());
        if (a.getCategoria() != null) {
            AlbumDTO.Cat c = new AlbumDTO.Cat();
            c.setId(a.getCategoria().getId());
            c.setNombre(a.getCategoria().getNombre());
            d.setCategoria(c);
        }
        d.setTags(a.getTags());
        d.setActivo(a.getActivo());
        d.setFechaLanzamiento(a.getFechaLanzamiento());
        d.setFechaSorteo(a.getFechaSorteo());
        d.setCantidadLaminas(a.getCantidadLaminas());
        return d;
    }
}
