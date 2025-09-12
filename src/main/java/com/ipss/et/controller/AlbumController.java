package com.ipss.et.controller;

import com.ipss.et.dto.AlbumCUDTO;
import com.ipss.et.dto.AlbumDTO;
import com.ipss.et.service.AlbumService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/album")
@CrossOrigin
@RequiredArgsConstructor
public class AlbumController {

    private final AlbumService service;

    @GetMapping("/")
    public List<AlbumDTO> listar() { return service.listar(); }

    @GetMapping("/{id}")
    public AlbumDTO obtener(@PathVariable Long id) { return service.obtener(id); }

    @PostMapping("/")
    public ResponseEntity<AlbumDTO> crear(@RequestBody AlbumCUDTO body) {
        return ResponseEntity.ok(service.crear(body));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AlbumDTO> actualizar(@PathVariable Long id, @RequestBody AlbumCUDTO body) {
        return ResponseEntity.ok(service.actualizar(id, body));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        service.desactivar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> activar(@PathVariable Long id) {
        service.activar(id);
        return ResponseEntity.noContent().build();
    }
}
