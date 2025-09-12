package com.ipss.et.service;

import com.ipss.et.dto.AlbumCUDTO;
import com.ipss.et.dto.AlbumDTO;
import com.ipss.et.model.*;
import com.ipss.et.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service @RequiredArgsConstructor
public class AlbumService {

    private final AlbumRepository albumes;
    private final CategoriaRepository categorias;
    private final LaminaRepository laminas;

    public List<AlbumDTO> listar() {
        return albumes.findAll().stream().map(AlbumDTO::of).toList();
    }

    public AlbumDTO obtener(Long id) {
        Album a = albumes.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Álbum no encontrado"));
        return AlbumDTO.of(a);
    }

    @Transactional
    public AlbumDTO crear(AlbumCUDTO in) {
        Album a = new Album();
        a.setNombre(in.getNombre());
        a.setFechaLanzamiento(in.getFechaLanzamiento());
        a.setFechaSorteo(in.getFechaSorteo());
        if (in.getCategoria()!=null && in.getCategoria().get("id")!=null) {
            a.setCategoria(categorias.findById(in.getCategoria().get("id"))
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Categoría inválida")));
        }
        a.setTags(in.getTags());
        a.setCantidadLaminas(in.getCantidadLaminas());
        a.setActivo(true);
        a = albumes.save(a);

        // Crear las láminas 1..N automáticamente
        int n = a.getCantidadLaminas()==null?0:a.getCantidadLaminas();
        for (int i=1;i<=n;i++) {
            Lamina l = Lamina.builder().numero(i).album(a).build();
            laminas.save(l);
        }
        return AlbumDTO.of(a);
    }

    @Transactional
    public AlbumDTO actualizar(Long id, AlbumCUDTO in) {
        Album a = albumes.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Álbum no encontrado"));

        if (in.getNombre()!=null) a.setNombre(in.getNombre());
        if (in.getFechaLanzamiento()!=null) a.setFechaLanzamiento(in.getFechaLanzamiento());
        if (in.getFechaSorteo()!=null) a.setFechaSorteo(in.getFechaSorteo());
        if (in.getCategoria()!=null && in.getCategoria().get("id")!=null) {
            a.setCategoria(categorias.findById(in.getCategoria().get("id"))
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Categoría inválida")));
        }
        if (in.getTags()!=null) a.setTags(in.getTags());

        if (in.getCantidadLaminas()!=null && !in.getCantidadLaminas().equals(a.getCantidadLaminas())) {
            int actual = Math.toIntExact(laminas.countByAlbumId(a.getId()));
            int nuevo = in.getCantidadLaminas();
            if (nuevo > actual) {
                for (int i=actual+1;i<=nuevo;i++) {
                    laminas.save(Lamina.builder().numero(i).album(a).build());
                }
            } else if (nuevo < actual) {
                for (int i=actual;i>nuevo;i--) {
                    laminas.findByAlbumIdAndNumero(a.getId(), i)
                           .ifPresent(laminas::delete);
                }
            }
            a.setCantidadLaminas(nuevo);
        }

        return AlbumDTO.of(albumes.save(a));
    }

    @Transactional public void desactivar(Long id) {
        Album a = albumes.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Álbum no encontrado"));
        a.setActivo(false);
        albumes.save(a);
    }

    @Transactional public void activar(Long id) {
        Album a = albumes.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Álbum no encontrado"));
        a.setActivo(true);
        albumes.save(a);
    }
}
