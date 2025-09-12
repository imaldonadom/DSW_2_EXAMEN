package com.ipss.et.controller;

import com.ipss.et.dto.ColeccionPayloads;
import com.ipss.et.service.ColeccionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/coleccion/coleccionista")
@CrossOrigin
@RequiredArgsConstructor
public class ColeccionController {

    private final ColeccionService service;

    @GetMapping("/")
    public List<ColeccionPayloads.ColeccionistaAlbumes> listar(@RequestParam Long id) {
        return service.listarPorColeccionista(id);
    }

    @GetMapping("/{id}")
    public List<ColeccionPayloads.ColeccionistaAlbumes> obtener(@PathVariable Long id) {
        return service.listarPorColeccionista(id);
    }

    @PostMapping("/{id}")
    public ResponseEntity<Void> crear(@PathVariable Long id,
                                      @RequestBody ColeccionPayloads.UpsertColeccion body) {
        service.upsert(id, body, false);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> reemplazar(@PathVariable Long id,
                                           @RequestBody ColeccionPayloads.UpsertColeccion body) {
        service.upsert(id, body, true);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarTodo(@PathVariable Long id) {
        service.deleteAll(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> reactivar(@PathVariable Long id,
                                          @RequestBody ColeccionPayloads.UpsertColeccion body) {
        service.upsert(id, body, false);
        return ResponseEntity.ok().build();
    }
}
