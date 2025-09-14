package com.ipss.et.controller;

import com.ipss.et.model.Album;
import com.ipss.et.model.Categoria;
import com.ipss.et.repository.AlbumRepository;
import com.ipss.et.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/v1/album")
@RequiredArgsConstructor
public class AlbumController {

    private final AlbumRepository albumes;
    private final CategoriaRepository categorias;

    // -------- GET: lista de álbumes --------
    @GetMapping
    public List<Map<String,Object>> list() {
        return albumes.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    // -------- POST: crear álbum --------
    @PostMapping
    public Map<String,Object> create(@RequestBody Map<String,Object> in) {
        Album a = new Album();
        a.setNombre((String) in.getOrDefault("nombre",""));

        // fechaLanzamiento y fechaSorteo en ISO (yyyy-MM-dd). Si vienen vacías, quedan en null.
        a.setFechaLanzamiento(parseDate((String) in.get("fechaLanzamiento")));
        a.setFechaSorteo(parseDate((String) in.get("fechaSorteo")));

        // tags: lista de strings (o vacío)
        @SuppressWarnings("unchecked")
        List<String> tags = (List<String>) in.get("tags");
        a.setTags(tags != null ? tags : new ArrayList<>());

        // activo (default true)
        Object act = in.get("activo");
        a.setActivo(act instanceof Boolean b ? b : Boolean.TRUE);

        // cantidadLaminas (default 0)
        Object cant = in.get("cantidadLaminas");
        a.setCantidadLaminas(cant instanceof Number n ? n.intValue() : 0);

        // categoría (obligatoria)
        Object catId = in.get("categoriaId");
        if (catId == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Categoría requerida");
        Long id = (catId instanceof Number n) ? n.longValue() : Long.valueOf(catId.toString());
        Categoria cat = categorias.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Categoría inválida"));
        a.setCategoria(cat);

        return toDto(albumes.save(a));
    }

    // -------- helpers --------
    private LocalDate parseDate(String s) {
        if (s == null || s.isBlank()) return null;
        return LocalDate.parse(s); // yyyy-MM-dd
    }

    private Map<String,Object> toDto(Album a) {
        Map<String,Object> cat = null;
        if (a.getCategoria()!=null) {
            cat = Map.of("id", a.getCategoria().getId(), "nombre", a.getCategoria().getNombre());
        }
        return Map.<String,Object>of(
                "id", a.getId(),
                "nombre", a.getNombre(),
                "categoria", cat,
                "activo", Boolean.TRUE.equals(a.getActivo()),
                "fechaLanzamiento", a.getFechaLanzamiento(),
                "fechaSorteo", a.getFechaSorteo(),
                "cantidadLaminas", a.getCantidadLaminas()==null?0:a.getCantidadLaminas(),
                "tags", a.getTags()==null? List.of(): a.getTags()
        );
    }
}
