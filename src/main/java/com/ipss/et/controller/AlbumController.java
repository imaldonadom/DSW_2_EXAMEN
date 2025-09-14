package com.ipss.et.controller;

import com.ipss.et.dto.AlbumCUDTO;
import com.ipss.et.dto.AlbumDTO;
import com.ipss.et.model.Album;
import com.ipss.et.model.Categoria;
import com.ipss.et.repository.AlbumRepository;
import com.ipss.et.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/album")
@RequiredArgsConstructor
public class AlbumController {

    private final AlbumRepository albumRepo;
    private final CategoriaRepository categoriaRepo;

    @GetMapping("/")
    public List<AlbumDTO> list() {
        return albumRepo.findAll()
                .stream().map(this::toDTO).toList();
    }

    @GetMapping("/{id}")
    public AlbumDTO get(@PathVariable Long id) {
        Album a = albumRepo.findById(id).orElseThrow();
        return toDTO(a);
    }

    @PostMapping("/")
    public AlbumDTO create(@RequestBody AlbumCUDTO dto) {
        Album a = new Album();
        apply(a, dto);
        a = albumRepo.save(a);
        return toDTO(a);
    }

    @PutMapping("/{id}")
    public AlbumDTO update(@PathVariable Long id, @RequestBody AlbumCUDTO dto) {
        Album a = albumRepo.findById(id).orElseThrow();
        apply(a, dto);
        a = albumRepo.save(a);
        return toDTO(a);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        albumRepo.deleteById(id);
    }

    // ---------- helpers ----------
    private void apply(Album a, AlbumCUDTO dto) {
        a.setNombre(dto.getNombre());

        // fechas (LocalDate) ya convertidas por el conversor
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
