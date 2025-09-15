package com.ipss.et.controller;

import com.ipss.et.repository.TipoLaminaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/tipo-lamina")
@RequiredArgsConstructor
public class TipoLaminaController {

    private final TipoLaminaRepository tipos;

    @GetMapping
    public List<Map<String, Object>> list() {
        return tipos.findAll().stream()
                .map(t -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", t.getId());
                    m.put("nombre", t.getNombre());
                    return m;
                })
                .toList();
    }
}
