package com.ipss.et.controller;

import com.ipss.et.model.Album;
import com.ipss.et.model.ColeccionItem;
import com.ipss.et.model.Lamina;
import com.ipss.et.repository.AlbumRepository;
import com.ipss.et.repository.ColeccionItemRepository;
import com.ipss.et.repository.LaminaRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/coleccion")
@CrossOrigin
public class ColeccionController {

    private final AlbumRepository albumRepo;
    private final LaminaRepository laminaRepo;
    private final ColeccionItemRepository colRepo;

    public ColeccionController(AlbumRepository albumRepo,
                               LaminaRepository laminaRepo,
                               ColeccionItemRepository colRepo) {
        this.albumRepo = albumRepo;
        this.laminaRepo = laminaRepo;
        this.colRepo = colRepo;
    }

    // Listar detalle por álbum+coleccionista
    @GetMapping
    public List<ColeccionItem> list(@RequestParam Long albumId,
                                    @RequestParam Long coleccionistaId) {
        return colRepo.listByCollectorAndAlbum(coleccionistaId, albumId);
    }

    // Totales (capacidad, coleccionadas, duplicadas, faltan)
    @GetMapping("/totales")
    public Map<String, Integer> totales(@RequestParam Long albumId,
                                        @RequestParam Long coleccionistaId) {
        Album alb = albumRepo.findById(albumId).orElseThrow();
        int capacidad = Optional.ofNullable(alb.getCantidadLaminas()).orElse(0);

        List<ColeccionItem> items = colRepo.listByCollectorAndAlbum(coleccionistaId, albumId);
        int coleccionadas = (int) items.stream()
                .filter(it -> (it.getCantidad() != null && it.getCantidad() > 0))
                .count();
        int duplicadas = items.stream()
                .mapToInt(it -> Math.max(0, (it.getCantidad() == null ? 0 : it.getCantidad()) - 1))
                .sum();
        int faltan = Math.max(0, capacidad - coleccionadas);

        Map<String,Integer> out = new LinkedHashMap<>();
        out.put("capacidad", capacidad);
        out.put("coleccionadas", coleccionadas);
        out.put("duplicadas", duplicadas);
        out.put("faltan", faltan);
        return out;
    }

    // Crear / actualizar un item (unidad)
    @PostMapping
    @Transactional
    public ColeccionItem upsert(@RequestBody Map<String, Object> body) {
        Long albumId = ((Number) body.get("albumId")).longValue();
        Long laminaId = ((Number) body.get("laminaId")).longValue();
        Long coleccionistaId = ((Number) body.get("coleccionistaId")).longValue();
        Integer cantidad = ((Number) body.getOrDefault("cantidad", 0)).intValue();

        ColeccionItem it = colRepo
                .findOneByCollectorAlbumAndLamina(coleccionistaId, albumId, laminaId)
                .orElseGet(() -> {
                    ColeccionItem x = new ColeccionItem();
                    x.setColeccionistaId(coleccionistaId);
                    x.setAlbum(albumRepo.findById(albumId).orElseThrow());
                    x.setLamina(laminaRepo.findById(laminaId).orElseThrow());
                    x.setActivo(true);
                    return x;
                });
        it.setCantidad(cantidad);
        return colRepo.save(it);
    }

    // Carga masiva: [{numero, cantidad}, ...]
    @PostMapping("/bulk")
    @Transactional
    public Map<String,Integer> bulk(@RequestParam Long albumId,
                                    @RequestParam Long coleccionistaId,
                                    @RequestBody List<Map<String, Object>> items) {
        Album album = albumRepo.findById(albumId).orElseThrow();

        // === OJOOOO: aquí uso TU método ===
        List<Lamina> laminas = laminaRepo.findByAlbumIdOrderByNumeroAsc(albumId);

        Map<Integer, Long> numeroToId = laminas.stream()
                .collect(Collectors.toMap(Lamina::getNumero, Lamina::getId));

        int inserted=0, updated=0, skipped=0;

        for (Map<String, Object> row : items) {
            if (!row.containsKey("numero") || !row.containsKey("cantidad")) { skipped++; continue; }

            int numero   = ((Number) row.get("numero")).intValue();
            int cantidad = ((Number) row.get("cantidad")).intValue();

            Long laminaId = numeroToId.get(numero);
            if (laminaId == null) { skipped++; continue; }

            Optional<ColeccionItem> opt = colRepo
                    .findOneByCollectorAlbumAndLamina(coleccionistaId, albumId, laminaId);

            if (opt.isPresent()) {
                ColeccionItem it = opt.get();
                it.setCantidad(cantidad);
                colRepo.save(it);
                updated++;
            } else {
                ColeccionItem it = new ColeccionItem();
                it.setColeccionistaId(coleccionistaId);
                it.setAlbum(album);
                it.setLamina(laminaRepo.findById(laminaId).orElseThrow());
                it.setCantidad(cantidad);
                it.setActivo(true);
                colRepo.save(it);
                inserted++;
            }
        }

        Map<String,Integer> out = new LinkedHashMap<>();
        out.put("inserted", inserted);
        out.put("updated", updated);
        out.put("skipped", skipped);
        return out;
    }

    // +1 / -1
    @PatchMapping("/{id}")
    @Transactional
    public ResponseEntity<?> delta(@PathVariable Long id,
                                   @RequestParam Integer delta) {
        ColeccionItem it = colRepo.findById(id).orElseThrow();
        int c = Optional.ofNullable(it.getCantidad()).orElse(0) + (delta == null ? 0 : delta);
        if (c < 0) c = 0;
        it.setCantidad(c);
        colRepo.save(it);
        return ResponseEntity.ok().build();
    }

    // Eliminar unidad
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (!colRepo.existsById(id)) return ResponseEntity.notFound().build();
        colRepo.deleteById(id);
        return ResponseEntity.ok().build();
    }

    
    @DeleteMapping("/album")
    @Transactional
    public ResponseEntity<?> deleteAlbum(@RequestParam Long albumId,
                                         @RequestParam Long coleccionistaId) {
        colRepo.deleteByCollectorAndAlbum(coleccionistaId, albumId);
        return ResponseEntity.ok().build();
    }
}
