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

    public List<ColeccionPayloads.ColeccionistaAlbumes> listarPorColeccionista(Long coleccionistaId) {
        var list = colecciones.findByColeccionistaId(coleccionistaId);

        Map<String,Object> colec = coleccionistas.findById(coleccionistaId)
                .map(c -> Map.<String,Object>of("id", c.getId(), "nombre", c.getNombre()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Coleccionista no existe"));

        var out = new ArrayList<ColeccionPayloads.ColeccionistaAlbumes.AlbumDeColeccion>();

        for (Coleccion c : list) {
            var lam = c.getLaminas().stream().map(ci ->
                    ColeccionPayloads.ColeccionistaAlbumes.LaminaCantidad.builder()
                            .id(ci.getLamina().getId())
                            .numero(ci.getLamina().getNumero())
                            .tipo(ci.getLamina().getTipoLamina()==null ? null :
                                    Map.<String,Object>of(
                                            "id", ci.getLamina().getTipoLamina().getId(),
                                            "nombre", ci.getLamina().getTipoLamina().getNombre()
                                    ))
                            .cantidad(ci.getCantidad())
                            .build()
            ).toList();

            var alb = c.getAlbum();

            Map<String,Object> cat = (alb.getCategoria()==null)
                    ? null
                    : Map.<String,Object>of(
                        "id", alb.getCategoria().getId(),
                        "nombre", alb.getCategoria().getNombre()
                      );

            Map<String,Object> estado = (c.getEstado()==null)
                    ? null
                    : Map.<String,Object>of(
                        "id", c.getEstado().getId(),
                        "nombre", c.getEstado().getNombre()
                      );

            out.add(ColeccionPayloads.ColeccionistaAlbumes.AlbumDeColeccion.builder()
                    .id(alb.getId())
                    .nombre(alb.getNombre())
                    .fechaDeLanzamiento(alb.getFechaLanzamiento()!=null ? alb.getFechaLanzamiento().toString() : null)
                    .fechaSorteo(alb.getFechaSorteo()!=null ? alb.getFechaSorteo().toString() : null)
                    .categoria(cat)
                    .tags(alb.getTags())
                    .cantidadDeLaminas(alb.getCantidadLaminas())
                    .laminas(lam)
                    .estado(estado)
                    .activo(c.getActivo())
                    .build());
        }

        return List.of(ColeccionPayloads.ColeccionistaAlbumes.builder()
                .coleccionista(colec)
                .album(out)
                .build());
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

            if (albIn.getEstado()!=null && albIn.getEstado().get("id")!=null) {
                c.setEstado(estados.findById(albIn.getEstado().get("id"))
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estado inválido")));
            }

            c = colecciones.save(c);

            for (var lamIn : albIn.getLaminas()) {
                Lamina lam = laminas.findById(lamIn.getId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lámina inválida"));
                var item = ColeccionItem.builder()
                        .coleccion(c).lamina(lam).cantidad(lamIn.getCantidad()).build();
                c.getLaminas().add(item); // cascade ALL en Coleccion mantiene la persistencia
            }

            // Guardamos cambios en la colección tras añadir items
            colecciones.save(c);
        }
    }

    @Transactional
    public void deleteAll(Long coleccionistaId) {
        colecciones.findByColeccionistaId(coleccionistaId).forEach(colecciones::delete);
    }
}
