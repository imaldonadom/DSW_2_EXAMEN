package com.ipss.et.controller;

import com.ipss.et.model.Album;
import com.ipss.et.model.Lamina;
import com.ipss.et.model.TipoLamina;
import com.ipss.et.repository.AlbumRepository;
import com.ipss.et.repository.LaminaRepository;
import com.ipss.et.repository.TipoLaminaRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/lamina")
@CrossOrigin(origins = "*")
public class LaminaCommandController {

    private final LaminaRepository laminaRepo;
    private final AlbumRepository albumRepo;
    private final TipoLaminaRepository tipoRepo;

    public LaminaCommandController(LaminaRepository laminaRepo,
                                   AlbumRepository albumRepo,
                                   TipoLaminaRepository tipoRepo) {
        this.laminaRepo = laminaRepo;
        this.albumRepo = albumRepo;
        this.tipoRepo = tipoRepo;
    }

    // ===== LISTAR (para que el front pueda usar /api/v1/lamina?albumId=) =====
    @GetMapping
    @Transactional(readOnly = true)
    public List<Map<String, Object>> listarPorAlbumV1(@RequestParam Long albumId) {
        return laminaRepo.findByAlbumIdOrderByNumeroAsc(albumId)
                .stream()
                .map(l -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", l.getId());
                    m.put("numero", l.getNumero());

                    Map<String, Object> alb = new LinkedHashMap<>();
                    alb.put("id", l.getAlbum().getId());
                    alb.put("nombre", l.getAlbum().getNombre());
                    m.put("album", alb);

                    Map<String, Object> tipo = null;
                    if (l.getTipo() != null) {
                        tipo = new LinkedHashMap<>();
                        tipo.put("id", l.getTipo().getId());
                        tipo.put("nombre", l.getTipo().getNombre());
                    }
                    m.put("tipo", tipo);
                    return m;
                })
                .toList();
    }

    // ===== GENERAR (el que te falta; acepta query params como usa tu JS) =====
        // Ej: POST /api/v1/lamina/generar?albumId=1&tipoId=2&cantidad=60
    @PostMapping("/generar")
    @Transactional
    public void generar(@RequestParam Long albumId,
                        @RequestParam(required = false) Long tipoId,
                        @RequestParam Integer cantidad) {

        if (cantidad == null || cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser > 0");
        }

        Album album = albumRepo.findById(albumId)
                .orElseThrow(() -> new IllegalArgumentException("Álbum no encontrado: " + albumId));

        TipoLamina tipo = null;
        if (tipoId != null) {
            tipo = tipoRepo.findById(tipoId)
                    .orElseThrow(() -> new IllegalArgumentException("Tipo no encontrado: " + tipoId));
        }

        // NUEVO: continuar desde el último número del álbum
        int start = laminaRepo.maxNumeroByAlbum(albumId) + 1;
        int end = start + cantidad - 1;

        for (int numero = start; numero <= end; numero++) {
            // por si hay huecos raros, igual protegemos
            if (laminaRepo.existsByAlbumIdAndNumero(albumId, numero)) continue;

            Lamina l = new Lamina();
            l.setAlbum(album);
            l.setNumero(numero);
            l.setTipo(tipo); // Dorada/Brillante/Normal según select (puede ser null)
            laminaRepo.save(l);
        }
    }


    // ===== ELIMINAR UNA =====
    @DeleteMapping("/{id}")
    @Transactional
    public void deleteOne(@PathVariable Long id) {
        laminaRepo.deleteById(id);
    }

    // ===== ELIMINAR SELECCIÓN =====
    @PostMapping(path = "/delete-selection", consumes = "application/json")
    @Transactional
    public Map<String, Object> deleteSelection(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) return Map.of("eliminados", 0);
        int n = laminaRepo.deleteByIdIn(ids);
        return Map.of("eliminados", n);
    }

    // ===== ELIMINAR TODAS POR ÁLBUM =====
    @DeleteMapping("/by-album/{albumId}")
    @Transactional
    public void deleteByAlbum(@PathVariable Long albumId) {
        laminaRepo.deleteByAlbumId(albumId);
    }
}
