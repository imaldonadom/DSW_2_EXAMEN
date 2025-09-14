package com.ipss.et.controller;

import com.ipss.et.dto.ColeccionPayloads;
import com.ipss.et.service.ColeccionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/coleccion")
@RequiredArgsConstructor
@CrossOrigin
public class ColeccionController {

    private final ColeccionService service;

    // Devuelve la lista de álbumes que tiene el coleccionista en su colección
    @GetMapping("/{coleccionistaId}")
    public List<ColeccionPayloads.ColeccionistaAlbumes.AlbumDeColeccion> listarPorColeccionista(
            @PathVariable Long coleccionistaId
    ) {
        return service.listarPorColeccionista(coleccionistaId);
    }

    // Alias opcional (si tenías otro endpoint similar, deja ambos apuntando al mismo tipo)
    @GetMapping("/{coleccionistaId}/albumes")
    public List<ColeccionPayloads.ColeccionistaAlbumes.AlbumDeColeccion> listarAlbumesDeColeccion(
            @PathVariable Long coleccionistaId
    ) {
        return service.listarPorColeccionista(coleccionistaId);
    }

    // Crea/actualiza la colección del coleccionista. Usa ?replace=true para reemplazar por completo
    @PutMapping("/{coleccionistaId}")
    public void upsert(
            @PathVariable Long coleccionistaId,
            @RequestBody ColeccionPayloads.UpsertColeccion body,
            @RequestParam(name = "replace", defaultValue = "false") boolean replace
    ) {
        service.upsert(coleccionistaId, body, replace);
    }

    // Elimina toda la colección del coleccionista
    @DeleteMapping("/{coleccionistaId}")
    public void deleteAll(@PathVariable Long coleccionistaId) {
        service.deleteAll(coleccionistaId);
    }
}
