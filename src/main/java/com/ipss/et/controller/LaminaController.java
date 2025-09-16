package com.ipss.et.controller;

import com.ipss.et.repository.LaminaRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class LaminaController {

    private final LaminaRepository laminaRepo;

    public LaminaController(LaminaRepository laminaRepo) {
        this.laminaRepo = laminaRepo;
    }

    @GetMapping("/laminas")
    @Transactional(readOnly = true)
    public List<Map<String, Object>> listarPorAlbum(@RequestParam Long albumId) {
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
}
