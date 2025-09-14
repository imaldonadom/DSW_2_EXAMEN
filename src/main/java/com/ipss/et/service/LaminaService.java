package com.ipss.et.service;

import com.ipss.et.dto.LaminaCUDTO;
import com.ipss.et.dto.LaminaDTO;
import com.ipss.et.model.Album;
import com.ipss.et.model.Lamina;
import com.ipss.et.model.TipoLamina;
import com.ipss.et.repository.AlbumRepository;
import com.ipss.et.repository.LaminaRepository;
import com.ipss.et.repository.TipoLaminaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LaminaService {

    private final LaminaRepository laminas;
    private final AlbumRepository albumes;
    private final TipoLaminaRepository tipos;

    @Transactional(readOnly = true)
    public List<LaminaDTO> listar(Long albumId) {
        List<Lamina> data = (albumId != null)
                ? laminas.findByAlbumIdOrderByNumeroAsc(albumId)
                : laminas.findAllByOrderByAlbumIdAscNumeroAsc();
        return data.stream().map(LaminaDTO::of).toList();
    }

    @Transactional
    public LaminaDTO crear(LaminaCUDTO in) {
        if (in.getAlbumId() == null) {
            throw new IllegalArgumentException("Álbum requerido");
        }
        Album alb = albumes.findById(in.getAlbumId())
                .orElseThrow(() -> new IllegalArgumentException("Álbum inválido"));

        TipoLamina tipo = null;
        if (in.getTipoId() != null) {
            tipo = tipos.findById(in.getTipoId())
                    .orElseThrow(() -> new IllegalArgumentException("Tipo inválido"));
        }

        Lamina l = Lamina.builder()
                .numero(in.getNumero() != null ? in.getNumero() : 1)
                .album(alb)
                .tipo(tipo)
                .build();

        return LaminaDTO.of(laminas.save(l));
    }

    @Transactional
    public void eliminar(Long id) {
        laminas.deleteById(id);
    }

    @Transactional
    public List<LaminaDTO> crearMasivo(List<LaminaCUDTO> items) {
        return items.stream().map(this::crear).toList();
    }
}
