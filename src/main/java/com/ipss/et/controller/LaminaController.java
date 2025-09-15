package com.ipss.et.controller;

import com.ipss.et.model.Album;
import com.ipss.et.model.Lamina;
import com.ipss.et.model.TipoLamina;
import com.ipss.et.service.LaminaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/lamina")
@RequiredArgsConstructor
public class LaminaController {

    private final LaminaService service;

    // Listado (DTO simple para evitar ByteBuddy/LAZY)
    @GetMapping
    public List<Map<String, Object>> list(@RequestParam Long albumId) {
        List<Lamina> data = service.listByAlbum(albumId);
        List<Map<String, Object>> out = new ArrayList<>(data.size());
        for (Lamina l : data) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", l.getId());
            m.put("numero", l.getNumero());

            Map<String, Object> alb = null;
            Album a = l.getAlbum();
            if (a != null) {
                alb = new LinkedHashMap<>();
                alb.put("id", a.getId());
                alb.put("nombre", a.getNombre());
            }
            m.put("album", alb);

            Map<String, Object> tip = null;
            TipoLamina t = l.getTipo();
            if (t != null) {
                tip = new LinkedHashMap<>();
                tip.put("id", t.getId());
                tip.put("nombre", t.getNombre());
            }
            m.put("tipo", tip);

            out.add(m);
        }
        return out;
    }

    // Generación
    @PostMapping("/generar")
    public Map<String, Integer> generar(@RequestParam Long albumId,
                                        @RequestParam Long tipoId,
                                        @RequestParam Integer cantidad) {
        return service.generar(albumId, tipoId, cantidad);
    }

    // Eliminar 1
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    // Eliminar selección
    @PostMapping("/delete-selection")
    public Map<String, Integer> deleteSelection(@RequestBody List<Long> ids) {
        int deleted = service.deleteSelection(ids);
        return Map.of("deleted", deleted);
    }

    // Eliminar todo por álbum
    @DeleteMapping("/by-album/{albumId}")
    public Map<String, Integer> deleteAllByAlbum(@PathVariable Long albumId) {
        int deleted = service.deleteAllByAlbum(albumId);
        return Map.of("deleted", deleted);
    }
}
