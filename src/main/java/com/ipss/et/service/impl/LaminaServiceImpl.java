package com.ipss.et.service.impl;

import com.ipss.et.model.Album;
import com.ipss.et.model.Lamina;
import com.ipss.et.model.TipoLamina;
import com.ipss.et.repository.AlbumRepository;
import com.ipss.et.repository.LaminaRepository;
import com.ipss.et.repository.TipoLaminaRepository;
import com.ipss.et.service.LaminaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Method;
import java.util.*;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
@RequiredArgsConstructor
public class LaminaServiceImpl implements LaminaService {

    private final LaminaRepository laminas;
    private final AlbumRepository albumes;
    private final TipoLaminaRepository tipos;

    // ------------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------------

    /** Lee capacidad desde getters comunes del Álbum. 0 => sin límite. */
    private int capacidad(Album a) {
        try {
            for (String m : new String[]{"getCantLaminas", "getCantidad", "getCapacidad", "getLaminas"}) {
                try {
                    Method mm = a.getClass().getMethod(m);
                    Object v = mm.invoke(a);
                    if (v instanceof Number n) return n.intValue();
                } catch (NoSuchMethodException ignored) { }
            }
        } catch (Exception ignored) { }
        return 0;
    }

    /** Siguientes números consecutivos libres, respetando capacidad si aplica. */
    private List<Integer> siguientesNumeros(Long albumId, int cuantos, Integer maxCap) {
        var existentes = laminas.findByAlbumIdOrderByNumeroAsc(albumId);
        Set<Integer> usados = new HashSet<>();
        for (var l : existentes) {
            if (l.getNumero() != null) usados.add(l.getNumero());
        }

        List<Integer> out = new ArrayList<>();
        int n = 1;
        while (out.size() < cuantos && (maxCap == null || n <= maxCap)) {
            if (!usados.contains(n)) out.add(n);
            n++;
        }
        return out;
    }

    // ------------------------------------------------------------------------
    // Implementación
    // ------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<Lamina> listByAlbum(Long albumId) {
        return laminas.findByAlbumIdOrderByNumeroAsc(albumId);
    }

    @Override
    @Transactional
    public Map<String, Integer> generar(Long albumId, Long tipoId, Integer cantidad) {
        Album a = albumes.findById(albumId)
                .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "Álbum inválido"));
        TipoLamina t = tipos.findById(tipoId)
                .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "Tipo inválido"));

        if (cantidad == null || cantidad <= 0) {
            throw new ResponseStatusException(BAD_REQUEST, "Cantidad inválida");
        }

        int cap = capacidad(a);                // 0 => sin límite
        Integer maxCap = (cap <= 0 ? null : cap);

        List<Integer> numeros = siguientesNumeros(albumId, cantidad, maxCap);

        int inserted = 0;
        for (Integer num : numeros) {
            Lamina l = new Lamina();
            l.setAlbum(a);
            l.setTipo(t);
            l.setNumero(num);
            laminas.save(l);
            inserted++;
        }
        int skipped = cantidad - inserted;

        return Map.of("inserted", inserted, "skipped", skipped);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (id == null) throw new ResponseStatusException(BAD_REQUEST, "ID inválido");
        if (!laminas.existsById(id)) {
            throw new ResponseStatusException(BAD_REQUEST, "Lámina no encontrada");
        }
        // ¡OJO! No retornar el resultado: deleteById es void.
        laminas.deleteById(id);
    }

    @Override
    @Transactional
    public int deleteSelection(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return 0;
        // deleteAllById es void: borramos y devolvemos el tamaño de la selección
        laminas.deleteAllById(ids);
        return ids.size();
    }

    @Override
    @Transactional
    public int deleteAllByAlbum(Long albumId) {
        if (albumId == null) throw new ResponseStatusException(BAD_REQUEST, "Álbum inválido");

        // Hacemos la cuenta ANTES, y luego borramos (da igual si el repo devuelve void/int/long)
        int count = laminas.findByAlbumIdOrderByNumeroAsc(albumId).size();

        // No hagas "return laminas.deleteByAlbumId(albumId);" si tu método es void.
        laminas.deleteByAlbumId(albumId);

        return count;
    }
}
