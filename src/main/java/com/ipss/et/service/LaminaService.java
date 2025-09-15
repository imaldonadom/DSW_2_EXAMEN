package com.ipss.et.service;

import com.ipss.et.model.Lamina;

import java.util.List;
import java.util.Map;

public interface LaminaService {

  Map<String, Integer> generar(Long albumId, Long tipoId, Integer cantidad);

  Map<String, Integer> bulkUpsert(Long albumId, List<Item> items);

  List<Lamina> list(Long albumId);

  void delete(Long id);

  // Item usado por el bulk (CSV o JSON)
  record Item(Integer numero, Long tipoId) {}
}
