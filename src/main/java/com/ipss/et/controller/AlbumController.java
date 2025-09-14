package com.ipss.et.controller;

import com.ipss.et.dto.AlbumCUDTO;
import com.ipss.et.dto.AlbumDTO;
import com.ipss.et.service.AlbumServiceImpl; // uso la impl para poder llamar a obtener(), pero puedes inyectar la interfaz si también la declara
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/album")
@RequiredArgsConstructor
public class AlbumController {

    private final AlbumServiceImpl service;

    @GetMapping
    public List<AlbumDTO> list() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public AlbumDTO get(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AlbumDTO create(@RequestBody AlbumCUDTO body) {
        return service.crear(body);
    }
}
