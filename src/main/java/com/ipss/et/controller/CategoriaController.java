package com.ipss.et.controller;

import com.ipss.et.dto.ComboDTO;
import com.ipss.et.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categoria")
@RequiredArgsConstructor
public class CategoriaController {
    private final CategoriaRepository repo;

    @GetMapping
    public List<ComboDTO> list() {
        return repo.findAll(Sort.by("id")).stream()
                .map(c -> new ComboDTO(c.getId(), c.getNombre()))
                .toList();
    }
}
