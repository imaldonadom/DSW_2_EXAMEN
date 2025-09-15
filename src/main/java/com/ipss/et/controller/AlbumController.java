package com.ipss.et.controller;

import com.ipss.et.dto.AlbumCUDTO;
import com.ipss.et.dto.AlbumDTO;
import com.ipss.et.service.AlbumService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/album")
@RequiredArgsConstructor
public class AlbumController {

    private final AlbumService service;

    @GetMapping
    public List<AlbumDTO> list() {
        return service.list();
    }

    @PostMapping
    public AlbumDTO create(@RequestBody AlbumCUDTO in) {
        return service.create(in);
    }

    @PutMapping("/{id}")
    public AlbumDTO update(@PathVariable Long id, @RequestBody AlbumCUDTO in) {
        return service.update(id, in);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
