package com.ipss.et.service;

import com.ipss.et.model.Lamina;

import java.util.List;
import java.util.Map;

public interface LaminaService {

    Map<String, Integer> generar(Long albumId, Long tipoId, Integer cantidad);

    List<Lamina> listByAlbum(Long albumId);

    /** Elimina 1 lámina por ID. */
    void delete(Long id);

    /** Elimina una selección de láminas por sus IDs. Devuelve cuántas se eliminaron. */
    int deleteSelection(List<Long> ids);

    /** Elimina TODAS las láminas de un álbum. Devuelve cuántas se eliminaron. */
    int deleteAllByAlbum(Long albumId);
}
