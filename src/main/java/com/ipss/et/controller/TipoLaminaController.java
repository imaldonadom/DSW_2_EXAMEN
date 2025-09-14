package com.ipss.et.controller;

import com.ipss.et.model.TipoLamina;
import com.ipss.et.repository.TipoLaminaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tipo-lamina")
@RequiredArgsConstructor
public class TipoLaminaController {

    private final TipoLaminaRepository tipos;

    @GetMapping("/")
    public List<TipoLamina> listar() {
        return tipos.findAll();
    }
}
