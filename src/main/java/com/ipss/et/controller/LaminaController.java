package com.ipss.et.controller;

import com.ipss.et.dto.LaminaCUDTO;
import com.ipss.et.dto.LaminaDTO;
import com.ipss.et.service.LaminaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/lamina")
@CrossOrigin
@RequiredArgsConstructor
public class LaminaController {

    private final LaminaService service;

    @GetMapping("/")
    public List<LaminaDTO> listar() { return service.listar(); }

    @GetMapping("/{id}")
    public LaminaDTO obtener(@PathVariable Long id) { return service.obtener(id); }

    @PostMapping("/")
    public ResponseEntity<LaminaDTO> crear(@RequestBody LaminaCUDTO body) {
        return ResponseEntity.ok(service.crear(body));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LaminaDTO> actualizar(@PathVariable Long id, @RequestBody LaminaCUDTO body) {
        return ResponseEntity.ok(service.actualizar(id, body));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
