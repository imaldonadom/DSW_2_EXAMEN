package com.ipss.et.service;

import com.ipss.et.dto.LaminaCUDTO;
import com.ipss.et.dto.LaminaDTO;
import com.ipss.et.model.*;
import com.ipss.et.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service @RequiredArgsConstructor
public class LaminaService {

    private final LaminaRepository laminas;
    private final AlbumRepository albumes;
    private final TipoLaminaRepository tipos;

    public List<LaminaDTO> listar() {
        return laminas.findAll().stream().map(LaminaDTO::of).toList();
    }

    public LaminaDTO obtener(Long id) {
        Lamina l = laminas.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Lámina no encontrada"));
        return LaminaDTO.of(l);
    }

    public LaminaDTO crear(LaminaCUDTO in) {
        Lamina l = new Lamina();
        l.setNumero(in.getNumero());
        if (in.getTipo()!=null && in.getTipo().get("id")!=null) {
            l.setTipoLamina(tipos.findById(in.getTipo().get("id"))
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tipo de lámina inválido")));
        }
        Long albumId = in.getAlbum()==null?null: in.getAlbum().get("id");
        if (albumId==null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Album requerido");
        l.setAlbum(albumes.findById(albumId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Álbum inválido")));
        return LaminaDTO.of(laminas.save(l));
    }

    public LaminaDTO actualizar(Long id, LaminaCUDTO in) {
        Lamina l = laminas.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Lámina no encontrada"));
        if (in.getNumero()!=null) l.setNumero(in.getNumero());
        if (in.getTipo()!=null && in.getTipo().get("id")!=null) {
            l.setTipoLamina(tipos.findById(in.getTipo().get("id"))
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tipo de lámina inválido")));
        }
        if (in.getAlbum()!=null && in.getAlbum().get("id")!=null) {
            l.setAlbum(albumes.findById(in.getAlbum().get("id"))
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Álbum inválido")));
        }
        return LaminaDTO.of(laminas.save(l));
    }

    public void eliminar(Long id) { laminas.deleteById(id); }
}
