package com.ipss.et.service;

import java.util.List;

public interface ColeccionService {

    // Fila para la vista consolidada
    class Row {
        public Long laminaId;
        public Integer numero;
        public String tipo;
        public Integer cantidad;
        public Integer duplicadas;
        public boolean falta;
    }

    // Item para guardar
    class ItemQ {
        public Long laminaId;
        public Integer cantidad;
    }

    List<Row> view(Long coleccionistaId, Long albumId);

    int bulkSet(Long coleccionistaId, Long albumId, List<ItemQ> items);

    int inc(Long coleccionistaId, Long albumId, Long laminaId, int delta);

    int clearAlbum(Long coleccionistaId, Long albumId);
}
