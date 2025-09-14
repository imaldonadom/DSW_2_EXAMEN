package com.ipss.et.controller;

import com.ipss.et.dto.LaminaCUDTO;
import com.ipss.et.dto.LaminaDTO;
import com.ipss.et.service.LaminaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/lamina")
@RequiredArgsConstructor
public class LaminaController {

    private final LaminaService service;

    @GetMapping("/")
    public List<LaminaDTO> listar(@RequestParam(value = "albumId", required = false) Long albumId) {
        return service.listar(albumId);
    }

    @PostMapping("/")
    @ResponseStatus(HttpStatus.CREATED)
    public LaminaDTO crear(@RequestBody LaminaCUDTO in) {
        return service.crear(in);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }

    // Carga masiva: recibe [{numero,albumId,tipoId}, ...]
    @PostMapping("/bulk")
    @ResponseStatus(HttpStatus.CREATED)
    public List<LaminaDTO> crearBulk(@RequestBody List<LaminaCUDTO> items) {
        return service.crearMasivo(items);
    }
}
