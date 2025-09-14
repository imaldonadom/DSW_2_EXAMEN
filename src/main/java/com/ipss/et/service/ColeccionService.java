package com.ipss.et.service;

import com.ipss.et.dto.ColeccionPayloads;
import com.ipss.et.model.*;
import com.ipss.et.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ColeccionService {

    private final ColeccionRepository colecciones;
    private final ColeccionistaRepository coleccionistas;
    private final AlbumRepository albumes;
    private final LaminaRepository laminas;
    private final EstadoRepository estados;

    @Transactional(readOnly = true)
    public List<ColeccionPayloads.ColeccionistaAlbumes.AlbumDeColeccion> listarPorColeccionista(Long coleccionistaId) {

        var list = colecciones.findByColeccionistaId(coleccionistaId);

        // valida existencia del coleccionista (por si te interesa devolver 404)
        coleccionistas.findById(coleccionistaId)
                .map(c -> Map.<String, Object>of("id", c.getId(), "nombre", c.getNombre()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Coleccionista no existe"));

        var out = new ArrayList<ColeccionPayloads.ColeccionistaAlbumes.AlbumDeColeccion>();

        for (Coleccion c : list) {

            // Construimos la lista con el tipo esperado por el builder: LaminaCantidad
            var lam = c.getLaminas().stream()
                    .map(ci -> {
                        var l = ci.getLamina();
                        // el campo tipo del DTO es un Map<String,Object>
                        var tipo = (l.getTipo() == null)
                                ? null
                                : Map.<String, Object>of(
                                        "id", l.getTipo().getId(),
                                        "nombre", l.getTipo().getNombre()
                                );

                        return ColeccionPayloads.ColeccionistaAlbumes.LaminaCantidad.builder()
                                .id(l.getId())
                                .numero(l.getNumero())
                                .tipo(tipo)
                                .cantidad(ci.getCantidad())
                                .build();
                    })
                    .toList();

            var alb = c.getAlbum();

            var cat = (alb.getCategoria() == null)
                    ? null
                    : Map.<String, Object>of(
                            "id", alb.getCategoria().getId(),
                            "nombre", alb.getCategoria().getNombre()
                    );

            out.add(
                    ColeccionPayloads.ColeccionistaAlbumes.AlbumDeColeccion.builder()
                            .id(alb.getId())
                            .nombre(alb.getNombre())
                            .categoria(cat)
                            .laminas(lam) // List<LaminaCantidad>
                            .build()
            );
        }

        return out;
    }

    @Transactional
    public void upsert(Long coleccionistaId, ColeccionPayloads.UpsertColeccion in, boolean replace) {
        Coleccionista col = coleccionistas.findById(coleccionistaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Coleccionista no existe"));

        if (replace) {
            colecciones.findByColeccionistaId(coleccionistaId).forEach(colecciones::delete);
        }

        for (var albIn : in.getAlbum()) {
            Album album = albumes.findById(albIn.getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Álbum inválido"));

            Coleccion c = Coleccion.builder()
                    .coleccionista(col)
                    .album(album)
                    .activo(Boolean.TRUE.equals(albIn.getActivo()))
                    .build();

            if (albIn.getEstado() != null && albIn.getEstado().get("id") != null) {
                c.setEstado(estados.findById(albIn.getEstado().get("id"))
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estado inválido")));
            }

            c = colecciones.save(c);

            for (var lamIn : albIn.getLaminas()) {
                Lamina lam = laminas.findById(lamIn.getId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lámina inválida"));

                var item = ColeccionItem.builder()
                        .coleccion(c)
                        .lamina(lam)
                        .cantidad(lamIn.getCantidad())
                        .build();

                c.getLaminas().add(item);
            }

            colecciones.save(c);
        }
    }

    @Transactional
    public void deleteAll(Long coleccionistaId) {
        colecciones.findByColeccionistaId(coleccionistaId).forEach(colecciones::delete);
    }
}
